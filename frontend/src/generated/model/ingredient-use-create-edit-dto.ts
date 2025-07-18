/**
 * merged spec
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { IngredientUnitDto } from './ingredient-unit-dto';


/**
 * Defines an ingredient by id and amount and unit.
 */
export interface IngredientUseCreateEditDto { 
    /**
     * Unique id of the ingredient used in the meal/recipe.
     */
    id: number;
    unit: IngredientUnitDto;
    /**
     * Amount of the ingredient used in regard to the unit.
     */
    amount: number;
}



