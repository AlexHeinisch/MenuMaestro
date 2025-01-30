package at.codemaestro.validation;

import org.openapitools.model.IngredientUnitDto;

public class IngredientConstraints {

    public static PropertyChecker validIngredientId(Long id) {
        return validIngredientId(id, "id");
    }

    public static PropertyChecker validIngredientId(Long id, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(id, propertyName).notNull().done();
    }

    public static PropertyChecker validIngredientAmount(Float amount) {
        return validIngredientAmount(amount, "amount");
    }

    public static PropertyChecker validIngredientAmount(Float amount, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(amount, propertyName).notNull().positive().done();
    }

    public static PropertyChecker validIngredientUnit(IngredientUnitDto unit) {
        return validIngredientUnit(unit, "unit");
    }

    public static PropertyChecker validIngredientUnit(IngredientUnitDto unit, String propertyName) {
        return PropertyChecker.begin()
            .checkThat(unit, propertyName).notNull().done();
    }

}
