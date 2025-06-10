package at.codemaestro.endpoint;

import at.codemaestro.exceptions.ConflictException;
import at.codemaestro.mapper.OrganizationMapper;
import at.codemaestro.persistence.MenuRepository;
import at.codemaestro.service.OrganizationService;
import at.codemaestro.validation.OrganizationConstraints;
import at.codemaestro.validation.UserConstraints;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.OrganizationsApi;
import org.openapitools.model.ChangeMemberRoleRequest;
import org.openapitools.model.InviteMemberRequest;
import org.openapitools.model.OrganizationMemberListPaginatedDto;
import org.openapitools.model.OrganizationRoleEnum;
import org.openapitools.model.OrganizationSummaryDto;
import org.openapitools.model.OrganizationSummaryListPaginatedDto;
import org.openapitools.model.RespondToInvitationRequest;

import at.codemaestro.exceptions.ValidationException;
import at.codemaestro.validation.PropertyChecker;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.OrganizationCreateDto;
import org.openapitools.model.OrganizationEditDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrganizationEndpoint implements OrganizationsApi {

    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'ADMIN')))")
    public ResponseEntity<Void> changeMemberRole(Long id, String username, ChangeMemberRoleRequest changeMemberRoleRequest) {
        log.info("PUT /organizations/{}/members/{}", id, username);
        log.debug("Request-Body: {}", changeMemberRoleRequest);
        PropertyChecker.begin().checkThat(changeMemberRoleRequest.getRole(), "role")
            .isNotIn(List.of(OrganizationRoleEnum.INVITED, OrganizationRoleEnum.OWNER))
            .done()
            .finalize(ValidationException::fromPropertyChecker);
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals(username)) {
            throw new ConflictException("Cannot change your own role!");
        }
        organizationService.changeRole(
            id,
            username,
            changeMemberRoleRequest.getRole()
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<OrganizationSummaryDto> createOrganization(OrganizationCreateDto organizationCreateDto) {
        log.info("POST /organizations");
        log.debug("Request-Body: {}", organizationCreateDto);

        validateOrganizationCreateDto(organizationCreateDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(organizationService.createOrganization(
                organizationCreateDto,
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString())
            );
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'MEMBER')))")
    public ResponseEntity<OrganizationSummaryDto> getOrganizationById(Long id) {
        log.info("GET /organizations/{}", id);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(organizationService.getOrganizationById(id));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'MEMBER')))")
    public ResponseEntity<OrganizationMemberListPaginatedDto> getOrganizationMembers(Long id, Integer page, Integer size) {
        log.info("GET /organizations/{}", id);

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(organizationMapper.mapMemberPageable(organizationService.getOrganizationMembers(id, p)));
    }


    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'ADMIN')))")
    public ResponseEntity<Void> inviteMember(Long id, InviteMemberRequest inviteMemberRequest) {
        log.info("POST /organizations/{}/members", id);
        log.debug("Request-Body: {}", inviteMemberRequest);
        PropertyChecker.begin().append(UserConstraints.validUsername(inviteMemberRequest.getUsername())).finalize(ValidationException::fromPropertyChecker);
        organizationService.inviteMemberToOrganization(id, inviteMemberRequest.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Void> respondToInvitation(Long id, RespondToInvitationRequest respondToInvitationRequest) {
        log.info("PUT /organizations/{}/members", id);
        log.debug("Request-Body: {}", respondToInvitationRequest);
        PropertyChecker.begin().checkThat(respondToInvitationRequest.getAccept(), "accept").notNull().done().finalize(ValidationException::fromPropertyChecker);
        organizationService.responseToInvitation(
            id,
            SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(),
            respondToInvitationRequest.getAccept()
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrganizationSummaryListPaginatedDto> getOrganizations(Integer page, Integer size, String name) {
        log.info("GET /organizations");
        log.info("Search-Params: name={} page={}, size={}", name, page, size);

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(organizationMapper.mapPageable(organizationService.getAllOrganizationsByNameSubstring(name, p)));
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(organizationMapper.mapPageable(organizationService.getOrganizationsByUsernameAndNameSubstring(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(),
                name,
                p
            )));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'ADMIN')))")
    public ResponseEntity<Void> removeMember(Long id, String username) {
        log.info("DELETE /organizations/{}/members/{}", id, username);
        PropertyChecker.begin().append(UserConstraints.validUsername(username)).finalize(ValidationException::fromPropertyChecker);
        organizationService.removeMember(
            id,
            username
        );
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().equals(username)) {
            throw new ConflictException("Cannot remove yourself!");
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'OWNER')))")
    public ResponseEntity<OrganizationSummaryDto> editOrganization(Long id, OrganizationEditDto organizationEditDto) {
        log.info("PUT /organizations/{}", id);
        log.debug("Request-Body: {}", organizationEditDto);
        return ResponseEntity.ok(organizationService.editOrganization(id, organizationEditDto));
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrganizationSummaryListPaginatedDto> getInvitations(Integer page, Integer size) {
        log.info("GET /organizations/invitations");
        log.debug("Search-Params: page={}, size={}", page, size);

        Pageable p = page == null && size == null
            ? Pageable.unpaged()
            : PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(organizationMapper.mapPageable(organizationService.getInvitationsByUsername(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString(),
                p
            )));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or ((hasRole('ROLE_USER') and @organizationService.hasPermissionForOrganization(#id, principal, 'OWNER')))")
    public ResponseEntity<Void> deleteOrganization(Long id) {
        log.info("DELETE /organizations/{}", id);
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    private void validateOrganizationCreateDto(OrganizationCreateDto organizationCreateDto) {
        PropertyChecker.begin()
            .append(OrganizationConstraints.validOrganizationName(organizationCreateDto.getName()))
            .append(OrganizationConstraints.validOptionalOrganizationDescription(organizationCreateDto.getDescription()))
            .finalize(ValidationException::fromPropertyChecker);
    }
}
