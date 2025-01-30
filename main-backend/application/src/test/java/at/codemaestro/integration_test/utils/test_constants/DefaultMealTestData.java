package at.codemaestro.integration_test.utils.test_constants;

import org.openapitools.model.MealEditDto;

public class DefaultMealTestData {

    public final static String DEFAULT_NEW_MEAL_NAME_1 = "This is a new meal name! 1";
    public final static Integer DEFAULT_NEW_NUMBER_OF_PEOPLE_1 = 33;

    public static MealEditDto defaultMealEditDto() {
        return new MealEditDto()
                .name(DEFAULT_NEW_MEAL_NAME_1)
                .numberOfPeople(DEFAULT_NEW_NUMBER_OF_PEOPLE_1);
    }
}
