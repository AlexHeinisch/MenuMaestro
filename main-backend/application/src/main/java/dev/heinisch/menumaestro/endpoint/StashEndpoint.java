package dev.heinisch.menumaestro.endpoint;

import dev.heinisch.menumaestro.exceptions.ValidationException;
import dev.heinisch.menumaestro.exceptions.VersionMatchFailedException;
import dev.heinisch.menumaestro.service.StashService;
import dev.heinisch.menumaestro.validation.PropertyChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.StashApi;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.StashResponseDto;
import org.openapitools.model.StashSearchResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StashEndpoint implements StashApi {

    private final StashService stashService;

    @Override
    @PreAuthorize("""
            hasRole('ROLE_ADMIN')
            or (hasRole('ROLE_USER')
                and @organizationService.hasPermissionsForStash(#id, authentication.principal, 'MEMBER'))
            """)
    public ResponseEntity<StashResponseDto> getStash(Long id) {
        log.info("GET /stash/{}", id);
        StashResponseDto stash = stashService.getStash(id);
        return ResponseEntity.ok().header("ETag", stash.getVersionNumber()).body(stash);
    }

    @Override
    @PreAuthorize("""
            hasRole('ROLE_ADMIN')
            or (hasRole('ROLE_USER')
                and @organizationService.hasPermissionsForStash(#id, authentication.principal, 'PLANNER'))
            """)
    public ResponseEntity<Void> updateStashIngredients(Long id, List<IngredientUseCreateEditDto> body, String ifMatch) {
        validateStashUpdateRequest(id, body);
        Long versionNumber = null;
        if (ifMatch != null) {
            try {
                versionNumber = Long.parseLong(ifMatch.replace("\"", ""));
            } catch (NumberFormatException e) {
                // noinspection deprecation // handled by our own exception mapper
                throw new HttpMessageNotReadableException("Invalid if-match header!");
            }
        }
        try {
            versionNumber = stashService.updateStash(id, body, versionNumber);
            return ResponseEntity.ok().header("ETag", versionNumber.toString()).build();
        } catch (VersionMatchFailedException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
    }

    @PreAuthorize("""
            hasRole('ROLE_ADMIN')
            or (hasRole('ROLE_USER')
                and @organizationService.hasPermissionsForStash(#id, authentication.principal, 'PLANNER')
                and @organizationService.hasPermissionsForStash(#otherStashId, authentication.principal, 'PLANNER'))
            """)
    @Override
    public ResponseEntity<Void> moveStashIngredients(Long id, Long otherStashId, List<IngredientUseCreateEditDto> body) {
        validateStashTransferRequest(id, otherStashId, body);
        Long versionNumber = stashService.moveStashIngredients(id, otherStashId, body);
        return ResponseEntity.noContent().header("ETag", versionNumber.toString()).build();
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<List<StashSearchResponseDto>> searchStashes(String name, Integer page, Integer size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (name == null) {
            name = "";
        }
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 10;
        }
        return ResponseEntity.ok(stashService.searchStashes(name, username, PageRequest.of(page, size)));
    }

    void validateStashUpdateRequest(Long id, List<IngredientUseCreateEditDto> body) {
        PropertyChecker.begin()
                .checkThat(id, "stash id").notNull().done()
                .checkThat(body, "body").notNull()
                .forEach((checker, dto) ->
                        checker.checkThat(dto.getId(), "ingredient id").notNull().done()
                                .checkThat(dto.getAmount(), "updated ingredient amount").notNull().notNegative().done()
                                .checkThat(dto.getUnit(), "ingredient unit").notNull().done())
                .done()
                .finalize(ValidationException::fromPropertyChecker);
    }

    void validateStashTransferRequest(Long id1, Long id2, List<IngredientUseCreateEditDto> body) {
        PropertyChecker.begin()
                .checkThat(id1, "stash id").notNull().done()
                .checkThat(id2, "other stash id").notNull().done()
                .checkThat(body, "body").notNull()
                .forEach((checker, dto) ->
                        checker.checkThat(dto.getId(), "ingredient id in list").notNull().done()
                                .checkThat(dto.getAmount(), "amount to transfer").notNull().positive().done()
                                .checkThat(dto.getUnit(), "ingredient unit").notNull().done())
                .done()
                .finalize(ValidationException::fromPropertyChecker);
    }
}
