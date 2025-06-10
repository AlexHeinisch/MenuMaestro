package at.codemaestro.mapper;

import at.codemaestro.domain.organization.Organization;

import at.codemaestro.domain.organization.OrganizationAccountRelation;
import at.codemaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.OrganizationCreateDto;
import org.openapitools.model.OrganizationMemberListPaginatedDto;
import org.openapitools.model.OrganizationSummaryDto;
import org.openapitools.model.OrganizationMemberDto;
import org.openapitools.model.OrganizationRoleEnum;
import org.openapitools.model.OrganizationSummaryListPaginatedDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrganizationMapper extends BasePageableMapper<OrganizationSummaryListPaginatedDto, OrganizationSummaryDto> {

    @Mapping(target = "stashId", source = "stash.id")
    OrganizationSummaryDto toOrganizationSummaryDto(Organization organization);

    List<OrganizationSummaryDto> toOrganizationSummaryDtos(List<Organization> organizations);

    @Mapping(target = "id", ignore = true)
    Organization toOrganization(OrganizationCreateDto organization);

    OrganizationCreateDto toOrganizationCreateDto(Organization organization, String username);

    @Mapping(target = "totalElements", source = "totalElements")
    OrganizationMemberListPaginatedDto mapMemberPageable(Page<OrganizationMemberDto> page);

    default List<OrganizationMemberDto> mapMembers(List<OrganizationAccountRelation> organizationAccountRelationList) {
        return organizationAccountRelationList.stream()
                .map(this::mapOrganizationAccountRelationToMember)
                .collect(Collectors.toList());
    }

    default OrganizationMemberDto mapOrganizationAccountRelationToMember(OrganizationAccountRelation organizationAccountRelation) {
        OrganizationMemberDto member = new OrganizationMemberDto();
        member.setUsername(organizationAccountRelation.getAccount().getUsername());
        member.email(organizationAccountRelation.getAccount().getEmail());
        member.role(OrganizationRoleEnum.fromValue(organizationAccountRelation.getRole().name().toLowerCase()));
        return member;
    }
}
