package at.codemaestro.integration_test.utils.test_constants;

import at.codemaestro.domain.cooking_appliance.CookingAppliance;

public class DefaultCookingApplianceTestData {
    public static final String DEFAULT_COOKING_APPLIANCE_NAME_1 = "appliance-Alpha";
    public static final String DEFAULT_COOKING_APPLIANCE_NAME_2 = "appliance-Beta";
    public static final String DEFAULT_COOKING_APPLIANCE_NAME_3 = "appliance-Gamma";
    public static final String DEFAULT_COOKING_APPLIANCE_NAME_4 = "appliance-Delta";

    public static CookingAppliance defaultCookingAppliance1() {
        return CookingAppliance.builder()
                .name(DEFAULT_COOKING_APPLIANCE_NAME_1)
                .build();
    }

    public static CookingAppliance defaultCookingAppliance2() {
        return CookingAppliance.builder()
                .name(DEFAULT_COOKING_APPLIANCE_NAME_2)
                .build();
    }

    public static CookingAppliance defaultCookingAppliance3() {
        return CookingAppliance.builder()
                .name(DEFAULT_COOKING_APPLIANCE_NAME_3)
                .build();
    }

    public static CookingAppliance defaultCookingAppliance4() {
        return CookingAppliance.builder()
                .name(DEFAULT_COOKING_APPLIANCE_NAME_4)
                .build();
    }

}
