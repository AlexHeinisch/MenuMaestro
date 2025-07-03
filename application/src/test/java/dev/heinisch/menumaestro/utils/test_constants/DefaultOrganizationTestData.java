package dev.heinisch.menumaestro.utils.test_constants;

import dev.heinisch.menumaestro.domain.organization.Organization;
import org.openapitools.model.OrganizationCreateDto;

public class DefaultOrganizationTestData {

    public static final String DEFAULT_ORG_NAME = "Test Organization Alpha";
    public static final String DEFAULT_ORG_DESCRIPTION = "Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum";
    public static final String DEFAULT_ORG_NAME_2 = "Other Test Organization";
    public static final String DEFAULT_ORG_DESCRIPTION_2 = "Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum";
    public static final String DEFAULT_ORG_NAME_3 = "Other Other 2 Test Organization";
    public static final String DEFAULT_ORG_DESCRIPTION_3 = "Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum Lorem ipsum";

    public static Organization defaultOrganization1() {
        return Organization.builder()
                .name(DEFAULT_ORG_NAME)
                .description(DEFAULT_ORG_DESCRIPTION)
                .build();
    }

    public static Organization defaultOrganization2() {
        return Organization.builder()
                .name(DEFAULT_ORG_NAME_2)
                .description(DEFAULT_ORG_DESCRIPTION_2)
                .build();
    }

    public static Organization defaultOrganization3() {
        return Organization.builder()
                .name(DEFAULT_ORG_NAME_3)
                .description(DEFAULT_ORG_DESCRIPTION_3)
                .build();
    }

    public static OrganizationCreateDto defaultCreateOrganizationDto() {
        return new OrganizationCreateDto()
                .name(DEFAULT_ORG_NAME)
                .description(DEFAULT_ORG_DESCRIPTION);
    }

    public static OrganizationCreateDto defaultCreateOrganizationDto2() {
        return new OrganizationCreateDto()
                .name(DEFAULT_ORG_NAME_2)
                .description(DEFAULT_ORG_DESCRIPTION_2);
    }

    public static OrganizationCreateDto defaultCreateOrganizationDto3() {
        return new OrganizationCreateDto()
                .name(DEFAULT_ORG_NAME_3)
                .description(DEFAULT_ORG_DESCRIPTION_3);
    }
}
