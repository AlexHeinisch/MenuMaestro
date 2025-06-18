package dev.heinisch.menumaestro.validation;

public class OrganizationConstraints {

    public static PropertyChecker validOrganizationName(String orgName) {
        return PropertyChecker.begin()
            .checkThat(orgName, "organization name")
            .notNull().notBlank().maxLength(50).done();
    }

    public static PropertyChecker validOptionalOrganizationDescription(String orgDescription) {
        return PropertyChecker.begin()
            .checkThat(orgDescription, "organization description")
            .maxLength(250).done();
    }
}
