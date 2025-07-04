import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputFieldComponent, InputType } from '../../../../components/Input/InputField';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../../globals/error.service';
import {
  CreateIngredientDto,
  IngredientCategory, IngredientDto,
  IngredientsApiService,
  IngredientUnitDto
} from '../../../../../generated';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { TokenService } from '../../../../security/token.service';

@Component({
    selector: 'app-request-ingredient-modal',
    imports: [
        RouterModule,
        CommonModule,
        FormsModule,
        InputFieldComponent,
    ],
    templateUrl: './request-ingredient-modal.component.html'
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
