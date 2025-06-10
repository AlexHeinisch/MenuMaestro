package at.codemaestro.validation;

import org.openapitools.model.CookingApplianceUseCreateEditDto;
import org.openapitools.model.IngredientUseCreateEditDto;
import org.openapitools.model.RecipeVisibility;

import java.util.List;

public class RecipeConstraints {

    public static PropertyChecker validRecipeName(String recipeName) {
        return PropertyChecker.begin()
            .checkThat(recipeName, "name").notBlank().maxLength(150).done();
    }

    public static PropertyChecker validOptionalRecipeDescription(String description) {
        return PropertyChecker.begin()
            .checkThat(description, "description").maxLength(1023).done();
    }

    public static PropertyChecker validServings(Integer servings) {
        return PropertyChecker.begin()
            .checkThat(servings, "servings").notNull().positive().done();
    }

    public static PropertyChecker validVisibility(RecipeVisibility visibility) {
        return PropertyChecker.begin()
            .checkThat(visibility, "visibility").notNull().done();
    }

    public static PropertyChecker validIngredientList(List<IngredientUseCreateEditDto> ingredients) {
        return PropertyChecker.begin()
            .checkThat(ingredients, "ingredients")
            .notNull()
            .notEmpty()
            .forEach((c, i) -> c
                .append(IngredientConstraints.validIngredientAmount(i.getAmount(), "amount in ingredient list"))
                .append(IngredientConstraints.validIngredientId(i.getId(), "id in ingredient list"))
                .append(IngredientConstraints.validIngredientUnit(i.getUnit(), "unit in ingredient list"))
            ).done();
    }

    public static PropertyChecker validCookingApplianceList(List<CookingApplianceUseCreateEditDto> cookingAppliances) {
        return PropertyChecker.begin()
            .checkThat(cookingAppliances, "cooking appliances")
            .notNull()
            .forEach((c, ca) -> c
                .append(CookingApplianceConstraints.validCookingApplianceAmount(ca.getAmount(), "amount in cooking appliance list"))
                .append(CookingApplianceConstraints.validCookingApplianceId(ca.getId(), "id in cooking appliance list"))
            ).done();
    }
}
