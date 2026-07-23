import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";
import { RouterModule } from "@angular/router";

import { FormsModule } from "@angular/forms";
import {
  InputFieldComponent,
  InputType,
} from "../../../../components/Input/InputField";
import { ToastrService } from "ngx-toastr";
import { ErrorService } from "../../../../globals/error.service";
import {
  CreateIngredientDto,
  IngredientCategory,
  IngredientDto,
  IngredientsApiService,
  IngredientUnitDto,
} from "../../../../../generated";
import { catchError, Observable, tap, throwError } from "rxjs";
import { TokenService } from "../../../../security/token.service";

@Component({
  selector: "app-request-ingredient-modal",
  imports: [RouterModule, FormsModule, InputFieldComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./request-ingredient-modal.component.html",
})
export class RequestIngredientModalComponent implements OnChanges {
  private ingredientsApiService = inject(IngredientsApiService);
  private toastr = inject(ToastrService);
  private errorService = inject(ErrorService);
  private tokenService = inject(TokenService);

  @Input() ingredientName: string = "";

  InputType = InputType;

  newIngredient: CreateIngredientDto = {} as CreateIngredientDto;

  measurementUnits = Object.values(IngredientUnitDto);
  categories = Object.values(IngredientCategory);

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes["ingredientName"] &&
      changes["ingredientName"].currentValue !== undefined
    ) {
      this.newIngredient = {
        name: this.ingredientName,
        defaultUnit: IngredientUnitDto.Grams,
        category: IngredientCategory.Other,
      };
    }
  }

  isAdmin(): boolean {
    return this.tokenService.isAdmin();
  }

  suggestIngredient(): Observable<IngredientDto> {
    return this.ingredientsApiService
      .suggestIngredient(this.newIngredient)
      .pipe(
        tap(() => {
          if (this.isAdmin()) {
            this.toastr.success("Ingredient created.");
          } else {
            this.toastr.success("Ingredient request sent.");
          }
        }),
        catchError((err) => {
          this.errorService.printErrorResponse(err);
          return throwError(() => err);
        }),
      );
  }
}
