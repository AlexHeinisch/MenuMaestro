package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.account.Account;
import dev.heinisch.menumaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.AccountCreateRequestDto;
import org.openapitools.model.AccountInfoDto;
import org.openapitools.model.AccountSummaryDto;
import org.openapitools.model.AccountSummaryListPaginatedDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AccountMapper extends BasePageableMapper<AccountSummaryListPaginatedDto, AccountSummaryDto> {

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordResetToken", ignore = true)
    @Mapping(target = "passwordResetPermittedUntil", ignore = true)
    @Mapping(target = "isGlobalAdmin", constant = "false")
    Account toEntity(AccountCreateRequestDto dto);

    AccountInfoDto toInfoDto(Account entity);

    AccountSummaryDto toSummaryDto(Account entity);
}
