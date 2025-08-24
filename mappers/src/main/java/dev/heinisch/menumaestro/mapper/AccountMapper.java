package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.domain.account.PendingAccount;
import dev.heinisch.menumaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.AccountCreateRequest;
import org.openapitools.model.AccountInfoResponse;
import org.openapitools.model.AccountSummaryListPaginatedResponse;
import org.openapitools.model.AccountSummaryResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper extends BasePageableMapper<AccountSummaryListPaginatedResponse, AccountSummaryResponse> {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isGlobalAdmin", constant = "false")
    @Mapping(target = "confirmationToken", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "lastTokenIssued", ignore = true)
    PendingAccount toEntity(AccountCreateRequest dto);

    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetPermittedUntil", ignore = true)
    Account toAccountEntity(PendingAccount pendingAccount);

    AccountInfoResponse toInfoDto(Account entity);

    AccountInfoResponse toInfoDto(PendingAccount entity);

    AccountSummaryResponse toSummaryDto(Account entity);
}
