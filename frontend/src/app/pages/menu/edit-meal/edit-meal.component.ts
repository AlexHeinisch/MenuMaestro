import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { map, Observable, tap } from "rxjs";
import { EditRecipeComponent } from "../../recipe/recipe-edit/recipe-edit.component";

import { IngredientComputationService } from "../../../service/ingredient-computation.service";
import { ToastrService } from "ngx-toastr";
import {
  IngredientUnitDto,
  MealEditDto,
  MealsApiService,
  RecipeCreateEditDto,
  RecipeDto,
} from "../../../../generated";

@Component({
  selector: "app-edit-meal-component",
  imports: [EditRecipeComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./edit-meal.component.html",
})
export class EditMealComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private toastr = inject(ToastrService);
  private mealsApiService = inject(MealsApiService);
  private ingredientComputationService = inject(IngredientComputationService);

  menuId: number | undefined;
  mealId: number | undefined;
  meal: MealEditDto | undefined;

  constructor() {
    console.log("constructor");
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.menuId = +params["menuId"];
      this.mealId = +params["mealId"];
    });
  }

  get redirectPath(): string {
    return `/menus/${this.menuId}/meal/${this.mealId}`;
  }

  fetchRecipeValueHandler(): Observable<RecipeDto> {
    return this.mealsApiService.getMealById(this.mealId!).pipe(
      map((meal) => {
        const scalingFactor = meal.numberOfPeople / meal.recipe.servings;
        meal.recipe.name = meal.name;
        meal.recipe.ingredients.forEach((i) => {
          i.amount = i.amount * scalingFactor;

          const [roundedAmount, newUnit] =
            this.ingredientComputationService.roundAmountForDisplay(
              i.amount,
              i.unit,
            );

          i.amount = roundedAmount;
          i.unit = newUnit as IngredientUnitDto;
        });
        meal.recipe.servings = meal.numberOfPeople;
        this.meal = meal;
        return meal.recipe as RecipeDto;
      }),
    );
  }

  editRecipeHandler(recipeEdit: RecipeCreateEditDto): Observable<any> {
    const meal = this.meal!;
    meal.recipe = recipeEdit;
    meal.name = recipeEdit.name;
    meal.numberOfPeople = recipeEdit.servings;
    return this.mealsApiService
      .editMealById(this.mealId!, meal)
      .pipe(tap((success) => this.toastr.success("Meal content edited.")));
  }
}
