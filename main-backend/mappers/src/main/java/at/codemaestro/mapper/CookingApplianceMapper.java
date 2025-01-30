package at.codemaestro.mapper;

import at.codemaestro.domain.cooking_appliance.CookingAppliance;
import at.codemaestro.mapper.util.BasePageableMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.openapitools.model.CookingApplianceDto;
import org.openapitools.model.CookingApplianceListPaginatedDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CookingApplianceMapper extends BasePageableMapper<CookingApplianceListPaginatedDto, CookingApplianceDto> {
    CookingAppliance toCookware(CookingApplianceDto cookware);

    CookingApplianceDto toCookwareDto(CookingAppliance cookingAppliance);

}
