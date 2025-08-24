package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.account.PendingAccount;
import dev.heinisch.menumaestro.domain.image.ImageRecord;
import dev.heinisch.menumaestro.domain.organization.OrganizationAccountRelation;
import dev.heinisch.menumaestro.domain.organization.OrganizationRole;
import dev.heinisch.menumaestro.domain.recipe.Recipe;
import dev.heinisch.menumaestro.domain.recipe.RecipeVisibility;
import dev.heinisch.menumaestro.domain.shopping_list.ShoppingListItem;
import dev.heinisch.menumaestro.exceptions.ConflictException;
import dev.heinisch.menumaestro.exceptions.ForbiddenException;
import dev.heinisch.menumaestro.exceptions.NotFoundException;
import dev.heinisch.menumaestro.mapper.AccountMapper;
import dev.heinisch.menumaestro.persistence.AccountRepository;
import dev.heinisch.menumaestro.persistence.ImageRepository;
import dev.heinisch.menumaestro.persistence.OrganizationAccountRelationRepository;
import dev.heinisch.menumaestro.persistence.PendingAccountRepository;
import dev.heinisch.menumaestro.persistence.RecipeRepository;
import dev.heinisch.menumaestro.persistence.ShoppingListItemRepository;
import dev.heinisch.menumaestro.properties.PasswordResetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AccountCreateRequest;
import org.openapitools.model.AccountEditRequest;
import org.openapitools.model.AccountInfoResponse;
import org.openapitools.model.AccountSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final PasswordResetProperties passwordResetProperties;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OrganizationAccountRelationRepository organizationAccountRelationRepository;
    private final RecipeRepository recipeRepository;
    private final OrganizationService organizationService;
    private final ImageRepository imageRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final PendingAccountRepository  pendingAccountRepository;

    @Transactional
    public AccountInfoResponse createAccount(AccountCreateRequest dto) {

        if (accountRepository.findById(dto.getUsername()).isPresent()
                || pendingAccountRepository.findById(dto.getUsername()).isPresent()) {
            throw new ConflictException(String.format("Username '%s' already exists!", dto.getUsername()));
        }
        if (accountRepository.findByEmail(dto.getEmail()).isPresent()
                || pendingAccountRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException(String.format("Email '%s' already exists!", dto.getEmail()));
        }

        PendingAccount newAccount = accountMapper.toEntity(dto);
        newAccount.setCreationDate(Instant.now());
        newAccount.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        this.sendConfirmationToken(dto.getUsername());

        pendingAccountRepository.save(newAccount);
        return accountMapper.toInfoDto(newAccount);
    }

    @Transactional
    public AccountInfoResponse editAccount(String username, AccountEditRequest dto) {
        if (dto.getEmail() != null) {
            Optional<Account> a = accountRepository.findByEmail(dto.getEmail());
            if (a.isPresent() && !a.get().getUsername().equals(username)) {
                throw new ConflictException(String.format("Email '%s' already exists!", dto.getEmail()));
            }
        }
        Account account = accountRepository.findById(username).orElseThrow(
            () -> new NotFoundException(String.format("Username '%s' not found!", username))
        );

        if (dto.getNewPassword() != null || dto.getEmail() != null) {
            if (!passwordEncoder.matches(dto.getOldPassword(), account.getPasswordHash())) {
                throw new ForbiddenException("Typed password does not match current password!");
            }
        }
        if (dto.getEmail() != null) {
            account.setEmail(dto.getEmail());
        }
        if (dto.getNewPassword() != null) {
            account.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        if (dto.getFirstName() != null) {
            account.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            account.setLastName(dto.getLastName());
        }
        return accountMapper.toInfoDto(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountSummaryResponse> searchUsersByAnyNameExcludingInOrganization(String name, Long excludedOrganizationId, Pageable pageable) {
        List<String> blacklistedUsernames = List.of();
        if (Objects.nonNull(excludedOrganizationId)) {
            blacklistedUsernames = organizationAccountRelationRepository.findMembersByOrganisationId(excludedOrganizationId)
                .stream()
                .map(OrganizationAccountRelation::getUsername)
                .toList();
        }

        return accountRepository.findMultipleUsernamesByAnyNameWithBlacklist(
            name,
            blacklistedUsernames,
            pageable
        ).map(accountMapper::toSummaryDto);
    }

    @Transactional
    public void initiatePasswordReset(String username) {
        Optional<Account> a = accountRepository.findById(username);
        if (a.isEmpty()) {
            // don't throw error to not give attackers insight on what is a username in use
            return;
        }
        Account account = a.get();

        String token = UUID.randomUUID().toString();
        account.setPasswordResetToken(token);
        account.setPasswordResetPermittedUntil(Instant.now().plus(passwordResetProperties.getExpirationTime()));
        emailService.sendPasswordResetEmail(account.getEmail(), token);
    }

    @Transactional
    public void commitPasswordReset(String username, String passwordResetToken, String newPassword) {
        Optional<Account> a = accountRepository.findById(username);
        if (a.isEmpty()) {
            // don't throw error to not give attackers insight on what is a username in use
            return;
        }
        Account account = a.get();
        if (account.getPasswordResetPermittedUntil() == null
            || account.getPasswordResetPermittedUntil().isBefore(Instant.now())
            || !account.getPasswordResetToken().equals(passwordResetToken)
        ) {
            throw new ForbiddenException("Password reset failed!"); // don't give too much information
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setPasswordResetToken(null);
        account.setPasswordResetPermittedUntil(null);
    }

    @Transactional
    public void deleteAccount(String username) {
        Account account = accountRepository.findById(username).orElseThrow(
            () -> new NotFoundException(String.format("Username '%s' not found!", username))
        );
        List<Recipe> authorOf = recipeRepository.findByAuthor(username).stream().toList();
        for (Recipe recipe : authorOf) {
            if (recipe.getVisibility() == RecipeVisibility.PRIVATE) {
                recipeRepository.delete(recipe);
            } else {
                recipe.getRecipeValue().setAuthor("Deleted User");
            }
        }
        List<OrganizationAccountRelation> memberOf = organizationAccountRelationRepository
            .findAllByUsername(username).stream().toList();
        for (OrganizationAccountRelation member : memberOf) {
            if (member.getRole() == OrganizationRole.OWNER) {
                List<OrganizationAccountRelation> members = organizationAccountRelationRepository
                    .findMembersByOrganisationId(member.getOrganizationId()).stream()
                    .filter(user -> user.getRole() != OrganizationRole.INVITED).toList();
                if (members.size() == 1) {
                    organizationService.deleteOrganization(member.getOrganizationId());
                } else {
                    List<OrganizationAccountRelation> admin =
                        members.stream().filter(user -> user.getRole() == OrganizationRole.ADMIN).toList();
                    if (!admin.isEmpty()) {
                        admin.getFirst().setRole(OrganizationRole.OWNER);
                    } else {
                        members.getFirst().setRole(OrganizationRole.OWNER);
                    }
                }
            }
            organizationAccountRelationRepository.delete(member);
        }
        List<ImageRecord> images = imageRepository.findImageRecordsByUploadedBy(username).stream().toList();
        for (ImageRecord image : images) {
            image.setUploadedBy("Deleted User");
        }
        List<ShoppingListItem> shoppingListItems = shoppingListItemRepository
            .findShoppingListItemsByCheckedByAccountUsername(username);
        for (ShoppingListItem shoppingListItem : shoppingListItems) {
            shoppingListItem.setCheckedByAccountUsername("Deleted User");
        }
        accountRepository.delete(account);
    }

    public AccountInfoResponse getAccountInfo(String username) {
        Account account = accountRepository.findById(username).orElseThrow();

        AccountInfoResponse accountInfo = new AccountInfoResponse();
        accountInfo.setUsername(account.getUsername());
        accountInfo.setFirstName(account.getFirstName());
        accountInfo.setLastName(account.getLastName());
        accountInfo.setEmail(account.getEmail());
        accountInfo.setIsGlobalAdmin(account.getIsGlobalAdmin());

        return accountInfo;
    }

    @Transactional
    public void confirmEmail(String username, String token) {
        PendingAccount pa = pendingAccountRepository.findById(username).orElseThrow(
                () -> new NotFoundException("Username not found or token invalid.")
        );
        if (!StringUtils.equals(pa.getConfirmationToken(), token)) {
            throw new NotFoundException("Username not found or token invalid.");
        }
        Account account = accountMapper.toAccountEntity(pa);
        accountRepository.save(account);
        pendingAccountRepository.delete(pa);
    }

    public void sendConfirmationToken(String username) {
        Optional<PendingAccount> opa = pendingAccountRepository.findById(username);
        if (opa.isEmpty()) {
            return;
        }
        var pa = opa.get();
        if (pa.getLastTokenIssued().isAfter(Instant.now().plus(15, ChronoUnit.MINUTES))) {
            return;
        }
        pa.setLastTokenIssued(Instant.now());
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        pa.setConfirmationToken(token);
        emailService.sendEmailConfirmationEmail(pa.getEmail(), username, token);
    }
}
