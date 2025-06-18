package dev.heinisch.menumaestro.validation;

public class ShoppingListConstraints {

    public static PropertyChecker validShoppingListName(String shoppingListName, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(shoppingListName, propertyName)
            .notNull().notBlank().done();
    }

    public static PropertyChecker validShoppingListName(String shoppingListName) {
            return validShoppingListName(shoppingListName, "name");
    }
}
