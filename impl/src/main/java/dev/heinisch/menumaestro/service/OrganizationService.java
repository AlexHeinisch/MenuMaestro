package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.organization.Organization;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.mapper.OrganizationMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.MenuRepository;
import dev.heinisch.menumaestro.persistence.OrganizationAccountRelationRepository;
import dev.heinisch.menumaestro.persistence.OrganizationRepository;
import dev.heinisch.menumaestro.persistence.StashRepository;
import dev.heinisch.menumaestro.persistence.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationMapper organizationMapper;
    private final AccountRepository accountRepository;
    private final MenuRepository menuRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccountRelationRepository organizationAccountRelationRepository;
    private final StashRepository stashRepository;
    private final MarkdownValidatorService markdownValidatorService;

    @Transactional(readOnly = true)
    public Page<OrganizationSummaryDto> getOrganizationsByUsernameAndNameSubstring(String username, String nameSearchQuery, Pageable page) {
        return organizationRepository.findByMemberUsernameAndNameContainingIgnoreCaseAndNotInvited(
            username,
            nameSearchQuery,
            page
        ).map(organizationMapper::toOrganizationSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationSummaryDto> getAllOrganizationsByNameSubstring(String nameSearchQuery, Pageable page) {
        return organizationRepository.findAllByNameContainingIgnoreCase(nameSearchQuery, page)
            .map(organizationMapper::toOrganizationSummaryDto);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationSummaryDto> getInvitationsByUsername(String username, Pageable page) {
        List<Long> organizationIds = organizationAccountRelationRepository.findAllByUsername(username)
            .stream()
            .filter(oar -> oar.getRole().equals(OrganizationRole.INVITED))
            .map(OrganizationAccountRelation::getOrganizationId)
            .toList();
        return organizationRepository.findAllByIdIn(organizationIds, page).map(organizationMapper::toOrganizationSummaryDto);
    }

    @Transactional(readOnly = true)
    public OrganizationSummaryDto getOrganizationById(Long organizationId) {
        return organizationMapper.toOrganizationSummaryDto(organizationRepository.findById(organizationId).orElseThrow(
            () -> NotFoundException.forEntityAndId("Organization", organizationId)
        ));
    }

    @Transactional(readOnly = true)
    public Page<OrganizationMemberDto> getOrganizationMembers(Long organizationId, Pageable p) {
        return organizationAccountRelationRepository.findMembersByOrganisationId(organizationId, p).map(organizationMapper::mapOrganizationAccountRelationToMember);
    }

    @Transactional(readOnly = true)
    public boolean hasPermissionForOrganization(long organizationId, String username, String role) {
        if (!organizationRepository.existsById(organizationId)) {
            return true; // will be handled by validation to return 404
        }
        var userRole = organizationAccountRelationRepository.findById(
                OrganizationAccountRelation.OrganizationAccountRelationId.builder()
                    .accountId(username)
                    .organizationId(organizationId)
                    .build()
            )
            .map(OrganizationAccountRelation::getRole)
            .orElseThrow(() -> new ForbiddenException("User not in the required organization!"));

        if (!userRole.isHigherOrEqualPermission(OrganizationRole.valueOf(role))) {
            throw new ForbiddenException(String.format("User has not enough permissions within the organization! (minimum-role: '%s')", role));
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean hasPermissionsForShoppingList(long shoppingListId, String username, String role) {
        return shoppingListRepository.findById(shoppingListId)
            .map(list -> hasPermissionForOrganization(list.getOrganizationId(), username, role))
            .orElse(true); // handle not found in method call not preauth
    }

    @Transactional(readOnly = true)
    public boolean hasPermissionsForMeal(long mealId, String username, String role) {
        var menu = menuRepository.findByMealId(mealId);
        if (Objects.isNull(menu)) {
            return true; // Not found gets handled by validation
        }
        return hasPermissionForOrganization(menu.getOrganizationId(), username, role);
    }

    @Transactional(readOnly = true)
    public boolean hasPermissionsForMenu(long menuId, String username, String role) {
        var menu = menuRepository.findById(menuId);
        // Not found gets handled by validation
        return menu.map(value -> hasPermissionForOrganization(value.getOrganizationId(), username, role)).orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean hasPermissionsForStash(long stashId, String username, String role) {
        Optional<Long> orgIdOpt = stashRepository.getOrganizationIdOfStash(stashId);
        return orgIdOpt.map(orgId -> hasPermissionForOrganization(orgId, username, role)).orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean doUsersShareOrganization(String username1, String username2) {
        Optional<Account> account1 = accountRepository.findById(username1);
        Optional<Account> account2 = accountRepository.findById(username2);
        if (account1.isEmpty() || account2.isEmpty()) {
            return false;
        }
        var organizationsOfAccount2 = organizationAccountRelationRepository.findByUsername(username2)
            .stream()
            .filter(o -> o.getRole() != OrganizationRole.INVITED)
            .map(o -> o.getId().getOrganizationId())
            .collect(Collectors.toSet());
        return organizationAccountRelationRepository.findByUsername(username1)
            .stream()
            .filter(o -> o.getRole() != OrganizationRole.INVITED)
            .map(o -> o.getId().getOrganizationId())
            .filter(organizationsOfAccount2::contains)
            .limit(1)
            .count() > 0;
    }


    @Transactional
    public OrganizationSummaryDto createOrganization(
        OrganizationCreateDto organizationCreateDto, String creatingUsername) {
        validateMarkdownDescription(organizationCreateDto.getDescription());
        if (organizationRepository.findByName(organizationCreateDto.getName()).isPresent()) {
            throw new ConflictException("An organization with that name already exists: " + organizationCreateDto.getName());
        }
        Account creatingAccount = accountRepository.findById(creatingUsername).orElseThrow(
            () -> new NotFoundException(String.format("Could not find account with username '%s'", creatingUsername))
        );

        Organization organization = organizationMapper.toOrganization(organizationCreateDto);
        OrganizationAccountRelation ownerRelation = OrganizationAccountRelation.builder()
            .organization(organization)
            .account(creatingAccount)
            .role(OrganizationRole.OWNER)
            .build();
        organization = organizationRepository.save(organization);
        organizationAccountRelationRepository.save(ownerRelation);
        return organizationMapper.toOrganizationSummaryDto(organization);
    }

    @Transactional
    public OrganizationSummaryDto editOrganization(long organizationId, OrganizationEditDto organizationEditDto) {
        validateMarkdownDescription(organizationEditDto.getDescription());
        Organization organization = organizationRepository.findById(organizationId).orElseThrow();
        organization.setName(organizationEditDto.getName());
        organization.setDescription(organizationEditDto.getDescription());
        return organizationMapper.toOrganizationSummaryDto(organization);
    }

    private void validateMarkdownDescription(String description) {
        try {
            markdownValidatorService.validateMarkdown(description);
        } catch (IllegalArgumentException e) {
            throw new dev.heinisch.menumaestro.exceptions.ValidationException(e.getMessage());
        }
    }


    @Transactional
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
            .orElseThrow(
                () -> new NotFoundException(String.format("Organization with id '%d' not found", id)));
        try {
            // Delete the associated organization account relations
            organizationAccountRelationRepository.deleteByOrganization(organization);

            menuRepository.deleteAllByOrganizationId(id);

            // Delete the organization
            organizationRepository.delete(organization);
        } catch (DataIntegrityViolationException ex) {
            // If the foreign key constraint violation occurs, throw a 409 Conflict
            if (ex.getMessage().contains("fk_menu_organization_id")) {
                throw new ConflictException("The organization is still referenced by menu items and cannot be deleted.");
            } else {
                throw new ConflictException("An unexpected conflict occurred while trying to delete the organization.");
            }
        }
    }

    @Transactional
    public void inviteMemberToOrganization(Long organizationId, String usernameToInvite) {
        Account accountToInvite = accountRepository.findById(usernameToInvite).orElseThrow(
            () -> new NotFoundException(String.format("Could not find account with username '%s'", usernameToInvite))
        );
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(
            () -> NotFoundException.forEntityAndId("Organization", organizationId)
        );
        if (organizationAccountRelationRepository.existsById(OrganizationAccountRelation.OrganizationAccountRelationId
            .builder()
            .organizationId(organizationId)
            .accountId(usernameToInvite)
            .build())
        ) {
            throw new ConflictException(String.format("Account with username '%s' already in organization with id '%d'", usernameToInvite, organization.getId()));
        }
        organizationAccountRelationRepository.save(OrganizationAccountRelation
            .builder()
            .role(OrganizationRole.INVITED)
            .organization(organization)
            .account(accountToInvite)
            .build()
        );
    }

    @Transactional
    public void responseToInvitation(Long organizationId, String respondingUsername, boolean hasAccepted) {
        if (!accountRepository.existsById(respondingUsername)) {
            throw new NotFoundException(String.format("Could not find account with username '%s'", respondingUsername));
        }
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(
            () -> NotFoundException.forEntityAndId("Organization", organizationId)
        );
        OrganizationAccountRelation relation = organizationAccountRelationRepository.findById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organizationId)
                .accountId(respondingUsername)
                .build())
            .orElseThrow(
                () -> new ConflictException(String.format("Account with username '%s' has not been invited to organization with id '%d'", respondingUsername, organization.getId()))
            );
        if (!relation.getRole().equals(OrganizationRole.INVITED)) {
            throw new ConflictException(String.format("Account with username '%s' is already member of organization with id '%d'", respondingUsername, organization.getId()));
        }
        if (hasAccepted) {
            relation.setRole(OrganizationRole.MEMBER);
        } else {
            organizationAccountRelationRepository.delete(relation);
        }
    }

    @Transactional
    public void removeMember(Long organizationId, String usernameToRemove) {
        if (!accountRepository.existsById(usernameToRemove)) {
            throw new NotFoundException(String.format("Could not find account with username '%s'", usernameToRemove));
        }
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(
            () -> NotFoundException.forEntityAndId("Organization", organizationId)
        );
        OrganizationAccountRelation relation = organizationAccountRelationRepository.findById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organizationId)
                .accountId(usernameToRemove)
                .build())
            .orElseThrow(
                () -> new ConflictException(String.format("Account with username '%s' is not part of organization with id '%d'", usernameToRemove, organization.getId()))
            );
        if (relation.getRole().equals(OrganizationRole.OWNER)) {
            throw new ConflictException("Cannot kick the owner of the organization!");
        }
        organizationAccountRelationRepository.delete(relation);
    }

    @Transactional
    public void changeRole(Long organizationId, String usernameToChangeRoleOf, OrganizationRoleEnum newRole) {
        if (!accountRepository.existsById(usernameToChangeRoleOf)) {
            throw new NotFoundException(String.format("Could not find account with username '%s'", usernameToChangeRoleOf));
        }
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(
            () -> NotFoundException.forEntityAndId("Organization", organizationId)
        );
        OrganizationAccountRelation relation = organizationAccountRelationRepository.findById(OrganizationAccountRelation.OrganizationAccountRelationId
                .builder()
                .organizationId(organizationId)
                .accountId(usernameToChangeRoleOf)
                .build())
            .orElseThrow(
                () -> new ConflictException(String.format("Account with username '%s' is not part of organization with id '%d'", usernameToChangeRoleOf, organization.getId()))
            );
        if (relation.getRole().equals(OrganizationRole.OWNER)) {
            throw new ConflictException("Cannot change the role of owner of the organization!");
        }
        relation.setRole(OrganizationRole.valueOf(newRole.name()));
    }
}
