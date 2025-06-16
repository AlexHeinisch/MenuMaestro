package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientComputationService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUnitConversionService;
import dev.heinisch.menumaestro.domain.ingredient_computation.IngredientUse;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.stash.Stash;
import dev.heinisch.menumaestro.domain.stash.StashEntry;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.exceptions.VersionMatchFailedException;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.mapper.StashMapper;
import dev.heinisch.menumaestro.persistence.EntityLockingRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import dev.heinisch.menumaestro.persistence.StashRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.StashResponseDto;
import org.openapitools.model.StashSearchResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StashService {

    private final StashRepository stashRepository;
    private final IngredientRepository ingredientRepository;
    private final StashMapper stashMapper;
    private final IngredientMapper ingredientMapper;
    private final EntityLockingRepository entityLocker;
    private final IngredientComputationService ingredientComputationService;
    private final IngredientUnitConversionService ingredientUnitConversionService;


    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public StashResponseDto getStash(Long stashId) {
        Stash stash = stashRepository.findByIdFetchAggregate(stashId)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", stashId));
        if (stash.getLocked()) {
            throw NotFoundException.forEntityAndId("Stash", stashId);
        }
        Set<Long> ingredientIds = stash.getEntries().stream()
                .map(StashEntry::getIngredientId)
                .collect(Collectors.toSet());
        Map<Long, Ingredient> ingredientsById = ingredientRepository.findAllById(ingredientIds)
                .stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));
        var stashMeta = stashRepository.getStashName(stashId).orElseThrow();
        return stashMapper.toStashResponseDto(stash, stashMeta.getName(), stashMeta.getOrganizationId(), ingredientsById);
    }

    @Transactional(readOnly = true)
    public List<StashSearchResponseDto> searchStashes(String queryName, String username, Pageable pageable) {
        Set<String> roles = Arrays.stream(OrganizationRole.values())
                .filter(or -> or.isHigherOrEqualPermission(OrganizationRole.PLANNER))
                .map(OrganizationRole::name)
                .collect(Collectors.toSet());
        var result = stashRepository.searchByName(queryName, username, roles, pageable);
        return stashMapper.toStashSearchDtoList(result.toList());
    }

    /**
     * Adds the given ingredients to the stash. Calling methods must use READ_COMMITTED isolation.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addToStash(Long stashId, List<IngredientUse> updateCommands) {
        entityLocker.lockEntity(Stash.class, stashId, 50)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", stashId));
        Stash stash = stashRepository.findByIdFetchAggregate(stashId).orElseThrow();
        if (stash.getLocked()) {
            throw new ConflictException("At least one of the stashes is locked.");
        }
        List<IngredientUse> ingredients = stash.getEntries().stream()
                .map(e -> IngredientUse.fromStashEntry(e, id -> ingredientRepository.findById(id).orElseThrow()))
                .collect(Collectors.toCollection(ArrayList::new));
        ingredients.addAll(updateCommands);
        List<IngredientUse> ingredientUses = ingredientComputationService.sumAutoconverting(ingredients)
                .filter(i -> i.amount() >= 0.001)
                .toList();
        var newStashEntries = ingredientUses.stream()
                .map(i -> StashEntry.builder()
                        .stash(stash)
                        .ingredientId(i.ingredient().getId())
                        .unit(i.unit())
                        .amount(i.amount())
                        .build())
                .collect(Collectors.toSet());
        stash.getEntries().clear();
        entityManager.flush(); // force delete statements to come first
        stash.getEntries().addAll(newStashEntries);
        stash.incrementVersionNumber();
    }

    /**
     * update the stash in a PATCH manner, i.e. treating the input as a list of modification commands.
     *
     * @param stashId        the id to update
     * @param updateCommands list of commands, for each given (ingredient, unit) pair will ensure the specified amount is in the stash, creating or deleting entries when appropriate.
     * @param versionNumber  if given, is checked against the database version.
     * @return a new versionNumber
     * @throws VersionMatchFailedException if the version numbers do not match.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = VersionMatchFailedException.class)
    public Long updateStash(Long stashId, List<IngredientUseCreateEditDto> updateCommands, Long versionNumber) throws VersionMatchFailedException {
        entityLocker.lockEntity(Stash.class, stashId, 50)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", stashId));
        Stash stash = stashRepository.findByIdFetchAggregate(stashId).orElseThrow();
        if (stash.getLocked()) {
            throw new ConflictException("Stash is locked.");
        }
        if (versionNumber != null && !stash.getVersionNumber().equals(versionNumber)) {
            throw new VersionMatchFailedException("Concurrent stash update detected.");
        }
        stash.incrementVersionNumber();
        Map<Pair<Long, IngredientUnit>, StashEntry> entryMap = stash.getEntries().stream()
                .collect(Collectors.toMap(this::toIngredientUseKey, Function.identity()));
        for (IngredientUseCreateEditDto updatedIngredientAmount : updateCommands) {
            applyStashUpdate(entryMap, stash, updatedIngredientAmount);
        }
        return stash.getVersionNumber();
    }

    void applyStashUpdate(Map<Pair<Long, IngredientUnit>, StashEntry> entryMap, Stash stash, IngredientUseCreateEditDto patch) {
        Pair<Long, IngredientUnit> key = toIngredientUseKey(patch);
        if (patch.getAmount() == 0) {
            // remove entry, if it still existed.
            StashEntry entry = entryMap.remove(key);
            if (entry != null) {
                stash.getEntries().remove(entry);
            } else {
                // shouldn't happen, log to be safe
                log.warn("Possible state mismatch: got request to remove ingredient not in stash.");
            }
        } else {
            StashEntry entry = entryMap.get(key);
            if (entry == null) {
                entry = StashEntry.builder()
                        .stash(stash)
                        .ingredientId(patch.getId())
                        .unit(ingredientMapper.toIngredientUnit(patch.getUnit()))
                        .amount((double) patch.getAmount())
                        .build();
                stash.getEntries().add(entry);
                entryMap.put(key, entry);
            } else {
                entry.setAmount((double) patch.getAmount());
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long moveStashIngredients(Long fromStashId, Long toStashId, List<IngredientUseCreateEditDto> transferAmounts) {
        entityLocker.lockEntity(Stash.class, fromStashId, 50)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Stash", fromStashId));
        entityLocker.lockEntity(Stash.class, toStashId, 50)
                .orElseThrow(() -> NotFoundException.forEntityAndId("Target Stash", toStashId));
        Stash fromStash = stashRepository.findByIdFetchAggregate(fromStashId).orElseThrow();
        if (fromStash.getLocked()) {
            throw new ConflictException("At least one of the stashes is locked.");
        }
        // toStash version incremented in addToStash()
        fromStash.incrementVersionNumber();
        Map<Pair<Long, IngredientUnit>, StashEntry> fromStashMap = fromStash.getEntries().stream()
                .collect(Collectors.toMap(this::toIngredientUseKey, Function.identity()));
        List<IngredientUse> ingredientsMoved = new ArrayList<>();
        for (IngredientUseCreateEditDto transferAmount : transferAmounts) {
            var key = toIngredientUseKey(transferAmount);
            StashEntry fromEntry = fromStashMap.get(key);
            if (fromEntry == null || fromEntry.getAmount() < transferAmount.getAmount()) {
                throw new ValidationException("Insufficient amount for ingredients, please check stash contents");
            } else if (fromEntry.getAmount() <= transferAmount.getAmount() + 0.0001) {
                fromStash.getEntries().remove(fromEntry);
                fromStashMap.remove(key);
            } else {
                fromEntry.setAmount(fromEntry.getAmount() - transferAmount.getAmount());
            }
            ingredientsMoved.add(new IngredientUse(ingredientRepository.findById(fromEntry.getIngredientId()).orElseThrow(),
                    fromEntry.getUnit(), transferAmount.getAmount()));
        }
        addToStash(toStashId, ingredientsMoved);
        return fromStash.getVersionNumber();
    }

    Pair<Long, IngredientUnit> toIngredientUseKey(StashEntry stashEntry) {
        return Pair.of(stashEntry.getIngredientId(), stashEntry.getUnit());
    }

    Pair<Long, IngredientUnit> toIngredientUseKey(IngredientUseCreateEditDto dto) {
        return Pair.of(dto.getId(), ingredientMapper.toIngredientUnit(dto.getUnit()));
    }
}
