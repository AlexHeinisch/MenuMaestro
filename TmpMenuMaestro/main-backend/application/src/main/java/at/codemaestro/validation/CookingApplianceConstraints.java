package at.codemaestro.validation;

public class CookingApplianceConstraints {

    public static PropertyChecker validCookingApplianceId(Long id) {
        return validCookingApplianceId(id, "Id");
    }

    public static PropertyChecker validCookingApplianceId(Long id, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(id, propertyName).notNull().done();
    }

    public static PropertyChecker validCookingApplianceAmount(Integer amount) {
        return validCookingApplianceAmount(amount, "Amount");
    }

    public static PropertyChecker validCookingApplianceAmount(Integer amount, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(amount, propertyName).notNull().positive().done();
    }

}
