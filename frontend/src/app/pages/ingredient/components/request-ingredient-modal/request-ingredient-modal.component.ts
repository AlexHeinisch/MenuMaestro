import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SimpleModalComponent } from '../../../../components/Modal/SimpleModalComponent';
import { InputFieldComponent, InputType } from '../../../../components/Input/InputField';
import { ButtonVariant, SimpleButtonComponent } from '../../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../../components/Input/SearchInput';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../../globals/error.service';
import { CreateIngredientDto } from '../../../../../generated/ingredients/model/create-ingredient-dto';
import { IngredientUnitDto } from '../../../../../generated/ingredients/model/ingredient-unit-dto';
import { IngredientCategory } from '../../../../../generated/ingredients/model/ingredient-category';
import { IngredientsApiService } from '../../../../../generated/ingredients/api/ingredients.service';
import { IngredientDto } from '../../../../../generated/ingredients/model/ingredient-dto';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { TokenService } from '../../../../security/token.service';

@Component({
  selector: 'app-request-ingredient-modal',
  standalone: true,
  imports: [
    RouterModule,
    CommonModule,
    FormsModule,
    SimpleModalComponent,
    InputFieldComponent,
    SimpleButtonComponent,
    SearchInputComponent,
  ],
  templateUrl: './request-ingredient-modal.component.html',
})
export class RequestIngredientModalComponent implements OnChanges {
  @Input() ingredientName: string = '';

  InputType = InputType;

  newIngredient: CreateIngredientDto = {} as CreateIngredientDto;

  constructor(
    private ingredientsApiService: IngredientsApiService,
    private toastr: ToastrService,
    private errorService: ErrorService,
    private tokenService: TokenService
  ) {}

  measurementUnits = Object.values(IngredientUnitDto);
  categories = Object.values(IngredientCategory);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['ingredientName'] && changes['ingredientName'].currentValue !== undefined) {
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
    return this.ingredientsApiService.suggestIngredient(this.newIngredient).pipe(
      tap(() => {
        if (this.isAdmin()) {
          this.toastr.success('Ingredient created.');
        } else {
          this.toastr.success('Ingredient request sent.');
        }
      }),
      catchError((err) => {
        this.errorService.printErrorResponse(err);
        return throwError(() => err);
      })
    );
  }
}
