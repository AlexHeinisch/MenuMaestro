package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.mapper.CookingApplianceMapper;
import dev.heinisch.menumaestro.service.CookingApplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.CookingAppliancesApi;
import org.openapitools.model.CookingApplianceListPaginatedDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class CookingApplianceEndpoint implements CookingAppliancesApi {

    private final CookingApplianceService cookingApplianceService;
    private final CookingApplianceMapper cookingApplianceMapper;

    @Override
    public ResponseEntity<CookingApplianceListPaginatedDto> getCookingAppliances(String name, Pageable pageable) {
        log.info("GET /cooking-appliances");
        log.debug("Search-Params: name='{}' page={}, size={}",
                name, pageable.getPageNumber(), pageable.getPageSize());

        var result = cookingApplianceService.getCookingAppliances(name, PageableHelper.orDefault(pageable));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(cookingApplianceMapper.mapPageable(result));
    }
}
