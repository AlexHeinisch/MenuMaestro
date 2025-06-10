package at.codemaestro.validation;

public class MenuConstraints {

    public static PropertyChecker validMenuName(String menuName) {
        return PropertyChecker.begin()
            .checkThat(menuName, "name").notBlank().maxLength(100).done();
    }

    public static PropertyChecker validSnapshotName(String menuName) {
        return PropertyChecker.begin()
            .checkThat(menuName, "name").notBlank().maxLength(100).done();
    }

    public static PropertyChecker validOptionalMenuDescription(String description) {
        return PropertyChecker.begin()
            .checkThat(description, "description").maxLength(1023).done();
    }

    public static PropertyChecker validNumberOfPeople(Integer servings) {
        return PropertyChecker.begin()
            .checkThat(servings, "number of people").notNull().positive().done();
    }

}
