package dev.heinisch.menumaestro.service;

import dev.heinisch.menumaestro.mapper.CookingApplianceMapper;
import dev.heinisch.menumaestro.persistence.CookingApplianceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.CookingApplianceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A service for everything related to cookware (CRUD + Search at the moment)
 */
@Service
@RequiredArgsConstructor
public class CookingApplianceService {

    private final CookingApplianceRepository cookingApplianceRepository;
    private final CookingApplianceMapper cookingApplianceMapper;

    @Transactional(readOnly = true)
    public Page<CookingApplianceDto> getCookingAppliances(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return cookingApplianceRepository.findAll(pageable).map(cookingApplianceMapper::toCookwareDto);
        }
        return cookingApplianceRepository.findCookwareByNameContainingIgnoreCase(name, pageable).map(cookingApplianceMapper::toCookwareDto);
    }
}
