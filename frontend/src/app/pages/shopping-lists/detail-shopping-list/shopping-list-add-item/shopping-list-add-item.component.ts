import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PageLayoutComponent } from '../../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../../components/Button/SimpleButton';
import { IngredientListPaginatedDto } from '../../../../../generated/ingredients/model/ingredient-list-paginated-dto';
import { IngredientsApiService } from '../../../../../generated/ingredients/api/ingredients.service';
import { IngredientDto } from '../../../../../generated/ingredients/model/ingredient-dto';
import { ShoppingListIngredientAddDto } from '../../../../../generated/shopping-lists/model/shopping-list-ingredient-add-dto';
import { IngredientUnitDto } from '../../../../../generated/shopping-lists/model/ingredient-unit-dto';
import { InputFieldComponent, InputType } from '../../../../components/Input/InputField';
import { SearchInputComponent } from '../../../../components/Input/SearchInput';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ShoppingListApiService } from '../../../../../generated/shopping-lists/api/shopping-list.service';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../../globals/error.service';
import { ShoppingListDto } from '../../../../../generated/shopping-lists/model/shopping-list-dto';
import { IngredientCategory } from '../../../../../generated/shopping-lists/model/ingredient-category';

@Component({
  selector: 'app-shopping-list-add-item',
  standalone: true,
  imports: [
    CommonModule,
    PageLayoutComponent,
    SimpleButtonComponent,
    InputFieldComponent,
    SearchInputComponent,
    FormsModule,
  ],
  templateUrl: './shopping-list-add-item.component.html',
})
export class ShoppingListAddItemComponent {
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  shoppingListIngredientAddDto: ShoppingListIngredientAddDto = {
    unit: IngredientUnitDto.Grams,
    amount: 1,
  };

  ingredientsOptions: IngredientDto[] = [];
  ingredientsOptionsNames: string[] = [];

  showRemainingFields: boolean = false;

  measurementUnits: IngredientUnitDto[] = Object.values(IngredientUnitDto);
  category: IngredientCategory = IngredientCategory.Other;

  @Input() shoppingListId: number = 1;
  @Input() shareToken: string | undefined = undefined;
  @Output() closeView = new EventEmitter<ShoppingListDto | null>();

  constructor(
    private ingredientsApiService: IngredientsApiService,
    private shoppingListApiService: ShoppingListApiService,
    private toastr: ToastrService,
    private errorService: ErrorService
  ) {}

  handleClick(): void {
    this.closeView.emit(null);
  }

  searchIngredient(searchTerm: string) {
    this.ingredientsApiService.searchIngredients(0, 5, undefined, searchTerm, this.shareToken).subscribe({
      next: (response: IngredientListPaginatedDto) => {
        if (response.content) {
          this.ingredientsOptions = response.content;
          this.ingredientsOptionsNames = this.ingredientsOptions.map((ingredient) => ingredient.name);
        } else {
          this.ingredientsOptions = [];
          this.ingredientsOptionsNames = [];
        }
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onIngredientSelected(selected: string) {
    const selectedIngredient = this.ingredientsOptions.find((ingredient) => ingredient.name === selected);

    if (selectedIngredient) {
      this.shoppingListIngredientAddDto.existingIngredientId = selectedIngredient.id;
      this.shoppingListIngredientAddDto.unit = selectedIngredient.defaultUnit as IngredientUnitDto;
    }

    this.showRemainingFields = true;
  }

  onAddCustomSelected(selected: string) {
    this.shoppingListIngredientAddDto.customIngredientName = selected;
    this.showRemainingFields = true;
  }

  onSubmit(form: NgForm) {
    if (form.valid) {
      this.shoppingListApiService
        .addItemToShoppingList(this.shoppingListId, this.shoppingListIngredientAddDto, this.shareToken)
        .subscribe({
          next: (response) => {
            this.closeView.emit(response);
            this.toastr.success('Successfully added item to your shopping list!');
          },
          error: (error) => {
            this.errorService.printErrorResponse(error);
          },
        });
    } else {
      console.error('Form is invalid');
    }
  }
}
