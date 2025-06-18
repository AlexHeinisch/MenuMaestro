package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.ingredient.Ingredient;
import dev.heinisch.menumaestro.domain.ingredient.IngredientCategory;
import dev.heinisch.menumaestro.domain.ingredient.IngredientStatus;
import dev.heinisch.menumaestro.domain.ingredient.IngredientUnit;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.mapper.IngredientMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.IngredientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.CreateIngredientDto;
import org.openapitools.model.IngredientDto;
import org.openapitools.model.IngredientDtoWithCategory;
import org.openapitools.model.ReplaceIngredientRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    @Transactional
    public IngredientDto approveIngredient(Long ingredientId) {
        Ingredient ingredient=ingredientRepository.findById(ingredientId).orElseThrow();
        ingredient.setStatus(IngredientStatus.PUBLIC);
        String requesterEMail = getEmailOfRequestingUser(ingredient);
        if (requesterEMail != null) {
            emailService.ingredientAcceptNotification(requesterEMail, ingredient.getName());
        }
        return ingredientMapper.toIngredientDto(ingredient);
    }

    @Transactional
    public void deleteIngredient(Long ingredientId) {
        //ToDo Delete all Foreign Keys. Stash and shopping list are issues
        Ingredient ingredient=ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> NotFoundException.forEntityAndId("ingredient", ingredientId));
        String email = getEmailOfRequestingUser(ingredient);
        if (email != null) {
            emailService.sendIngredientRejectNotification(email, ingredient.getName(), null);
        }
        ingredientRepository.deleteIngredientAndReferences(ingredientId);

    }
    @Transactional
    public Page<IngredientDtoWithCategory> findAllRequestedIngredients(Pageable p) {
        return ingredientRepository.findAllRequested(p).map(ingredientMapper::toIngredientWithCategoryDto);
    }

    @Transactional
    public IngredientDto suggestIngredient(CreateIngredientDto createIngredientDto,String username){
        Account user=accountRepository.findById(username).orElseThrow();
        if (ingredientRepository.findOwnRequested(username).size()>=10){
            return null;
        }
        Ingredient ingredient = Ingredient
                .builder()
                .name(createIngredientDto.getName())
                .category(IngredientCategory.valueOf(createIngredientDto.getCategory().getValue()))
                .parent(null)
                .status(IngredientStatus.REQUESTED)
                .username(username)
                .defaultUnit(IngredientUnit.valueOf(createIngredientDto.getDefaultUnit().getValue()))
                .build();

        return ingredientMapper.toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public IngredientDto replaceIngredient(Long ingredientId, ReplaceIngredientRequest replaceIngredientRequest) {
        Ingredient willBeReplaced;
        Ingredient replaces;

        willBeReplaced=findIngredientById(ingredientId);
        replaces=findIngredientById(replaceIngredientRequest.getIngredientId());
        if (willBeReplaced==null || replaces==null) {
            throw new NotFoundException("Ingredient with id "+ingredientId+" not found!");
        }
        if (isIngredientStatusPublic(ingredientId)){
            throw new ValidationException("Ingredient with id "+ingredientId+" already exists!");
        }
        String replacementName = replaces.getName();
        String requesterEmail = getEmailOfRequestingUser(willBeReplaced);
        if (ingredientRepository.checkIfReplacementIsSafe(willBeReplaced.getId(),replaces.getId())){
            ingredientRepository.replaceAllIngredientReferences(willBeReplaced.getId(),replaces.getId());
        } else {
            ingredientRepository.deleteIngredientAndReferences(willBeReplaced.getId());
            replacementName = null;
        }
        if (requesterEmail != null) {
            emailService.sendIngredientRejectNotification(requesterEmail, willBeReplaced.getName(), replacementName);
        }
        return ingredientMapper.toIngredientDto(replaces);
    }

    private String getEmailOfRequestingUser(Ingredient ingredient) {
        return accountRepository.findById(ingredient.getUsername()).map(Account::getEmail).orElse(null);
    }

    @Transactional
    public IngredientDto suggestIngredientAsAdmin(CreateIngredientDto createIngredientDto){
        Ingredient ingredient = Ingredient
                .builder()
                .name(createIngredientDto.getName())
                .category(IngredientCategory.valueOf(createIngredientDto.getCategory().getValue()))
                .parent(null)
                .status(IngredientStatus.PUBLIC)
                .username(null)
                .defaultUnit(IngredientUnit.valueOf(createIngredientDto.getDefaultUnit().getValue()))
                .build();

        return ingredientMapper.toIngredientDto(ingredientRepository.save(ingredient));
    }

    @Transactional
    public Ingredient findIngredientById(Long ingredientId) {
        return ingredientRepository.findById(ingredientId).orElse(null);
    }

    @Transactional
    public boolean isIngredientStatusPublic(Long ingredientId) {
        return ingredientRepository.findById(ingredientId).orElseThrow().getStatus()== IngredientStatus.PUBLIC;
    }

    public Page<IngredientDto> searchIngredients(String name, Pageable pageable,String username) {
        if (StringUtils.isBlank(name)) {
            return ingredientRepository.findAllPublicAndOwnRequested(username,pageable).map(ingredientMapper::toIngredientDto);
        }
        return ingredientRepository.findIngredientAndOwnRequestedByNameContainingIgnoreCase(name,username, pageable)
                .map(a -> (Ingredient) a[0])
                .map(ingredientMapper::toIngredientDto);
    }
}
