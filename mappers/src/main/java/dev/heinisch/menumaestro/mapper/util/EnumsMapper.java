package dev.heinisch.menumaestro.mapper.util;

import dev.heinisch.menumaestro.domain.menu.MenuStatus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EnumsMapper {
    // map ALL -> null
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
    MenuStatus toMenuStatus(org.openapitools.model.MenuStatus menuStatus);
}
