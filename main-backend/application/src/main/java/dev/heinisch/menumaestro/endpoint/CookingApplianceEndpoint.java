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
    public ResponseEntity<CookingApplianceListPaginatedDto> getCookingAppliances(Integer page, Integer size, List<String> sort, String name, Pageable pageable) {
        log.info("GET /cooking-appliances");
        log.debug("Search-Params: name='{}' page={}, size={}",
            name, page, size);

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        var result = cookingApplianceService.getCookingAppliances(name, p);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cookingApplianceMapper.mapPageable(result));
    }
}
