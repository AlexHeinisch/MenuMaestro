package at.codemaestro.domain.organization;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OrganizationRole {
    OWNER(5),
    ADMIN(4),
    PLANNER(3),
    SHOPPER(2),
    MEMBER(1),
    INVITED(0);

    private final int value;

    public boolean isHigherOrEqualPermission(OrganizationRole role) {
        return this.getValue() >= role.getValue();
    }
}
