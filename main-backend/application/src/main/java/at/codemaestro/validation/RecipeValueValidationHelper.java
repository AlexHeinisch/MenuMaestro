package at.codemaestro.validation;

import org.openapitools.model.RecipeCreateEditDto;
import org.springframework.stereotype.Component;

@Component
public class RecipeValueValidationHelper {

    public PropertyChecker validationForRecipeValue(RecipeCreateEditDto dto) {
        return PropertyChecker.begin()
            .append(RecipeConstraints.validRecipeName(dto.getName()))
            .append(RecipeConstraints.validOptionalRecipeDescription(dto.getDescription()))
            .append(UserConstraints.validUsername(dto.getAuthor(), "author"))
            .append(RecipeConstraints.validServings(dto.getServings()))
            .append(RecipeConstraints.validVisibility(dto.getVisibility()))
            .append(RecipeConstraints.validIngredientList(dto.getIngredients()))
            .append(RecipeConstraints.validCookingApplianceList(dto.getCookingAppliances()));
    }
}
