package dev.heinisch.menumaestro.mapper;

import dev.heinisch.menumaestro.domain.cooking_appliance.CookingAppliance;
import dev.heinisch.menumaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.CookingApplianceDto;
import org.openapitools.model.CookingApplianceListPaginatedDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CookingApplianceMapper extends BasePageableMapper<CookingApplianceListPaginatedDto, CookingApplianceDto> {
    CookingAppliance toCookware(CookingApplianceDto cookware);

    CookingApplianceDto toCookwareDto(CookingAppliance cookingAppliance);

}
