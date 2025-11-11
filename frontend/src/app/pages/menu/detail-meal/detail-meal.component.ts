import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant } from '../../../components/Button/SimpleButton';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { IngredientComputationService } from '../../../service/ingredient-computation.service';
import { ErrorService } from '../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { ComplexModalComponent } from '../../../components/Modal/ComplexModalComponent';
import { TokenService } from '../../../security/token.service';
import { MarkdownViewerComponent } from '../../../components/Markdown/MarkdownViewer/markdown-viewer.component';
import {IngredientUnitDto, MealDto, MealEditDto, MealsApiService, MealStatus} from "../../../../generated";

@Component({
    selector: 'app-detail-meal',
    imports: [
        RouterModule,
        CommonModule,
        PageLayoutComponent,
        SimpleButtonComponent,
        FormsModule,
        LoadingSpinnerComponent,
        SimpleButtonComponent,
        SimpleModalComponent,
        ComplexModalComponent,
        InputFieldComponent,
        MarkdownViewerComponent,
    ],
    templateUrl: './detail-meal.component.html'
})
export class DetailMealComponent implements OnInit {
  MealStatus = MealStatus;
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  mealId: number | null = null;
  menuId: number | null = null;
  meal: any = null;
  loadingMeal: boolean = true;

  mealDto: MealDto | undefined;

  deleteModalTitle: string = '';
  isDeleteModalOpen: boolean = false;
  isEditModalOpen: boolean = false;
  isEditModalEditName: boolean = true;

  mealEditDto: MealEditDto = { name: '', numberOfPeople: 1 };
  isCloseMealModalOpen: boolean = false;
  closeMealModalOpenIsDone: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private mealsApiService: MealsApiService,
    private ingredientComputationService: IngredientComputationService,
    private router: Router,
    private errorService: ErrorService,
    private toastrService: ToastrService,
    protected tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.menuId = +params['menuId'];
      this.mealId = +params['mealId'];
      this.fetchMeal(this.mealId);
    });
  }

  fetchMeal(mealId: number): void {
    this.mealsApiService.getMealById(mealId).subscribe({
      next: (meal: MealDto) => {
        this.loadingMeal = false;
        this.mealDto = meal;
        this.setDefaultNumberOfPeopleAndDefaultMealEditDtoValues();
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  setDefaultNumberOfPeopleAndDefaultMealEditDtoValues(): void {
    if (this.mealDto !== undefined) {
      this.mealEditDto.numberOfPeople = this.mealDto.numberOfPeople;
      if (this.mealDto.name != null) {
        this.mealEditDto.name = this.mealDto.name;
      }
    }
  }

  calculateAmount(ingredientAmountInRecipe: number, unit: IngredientUnitDto): string {
    const mealNumberOfPeople = this.mealDto?.numberOfPeople;
    const recipeNumberOfPeople = this.mealDto?.recipe?.servings;

    if (mealNumberOfPeople && recipeNumberOfPeople) {
      const result = ingredientAmountInRecipe * (mealNumberOfPeople / recipeNumberOfPeople);
      return this.ingredientComputationService.roundAmountForDisplayString(result, unit);
    }

    return ingredientAmountInRecipe + this.ingredientComputationService.formatUnitDisplay(unit);
  }

  formatStatus(status: string | undefined): string {
    if (!status) return 'Unknown Status';

    return status
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
  }
  closeMealModal(done: boolean) {
    this.closeMealModalOpenIsDone = done;
    this.isCloseMealModalOpen = true;
  }
  onMark(removeFromstash: boolean) {
    if (this.mealId !== null) {
      this.mealsApiService.markCompleted(this.mealId, this.closeMealModalOpenIsDone, removeFromstash).subscribe({
        next: () => {
          if (this.closeMealModalOpenIsDone) {
            this.toastrService.success(
              'Meal marked as done. \n This meal will be skipped for shopping lists and stash calculations.'
            );
            this.router.navigate([`/menus/${this.menuId}`]);
          } else {
            this.toastrService.success(
              'Meal marked as not done. \n This meal will be included in shopping lists and stash calculations.'
            );
            this.fetchMeal(this.mealId!);
          }
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  onEdit(successMessage: string) {
    if (this.mealId !== null) {
      this.mealsApiService.editMealById(this.mealId, this.mealEditDto).subscribe({
        next: (response) => {
          this.mealDto = response;
          this.setDefaultNumberOfPeopleAndDefaultMealEditDtoValues();
          this.toastrService.success(successMessage);
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  onDelete() {
    if (this.mealId !== null) {
      this.mealsApiService.deleteMealById(this.mealId).subscribe({
        next: () => {
          this.router.navigate([`/menus/${this.menuId}`]);
          this.toastrService.success('Meal deleted.');
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  openDeleteModal(): void {
    if (this.mealDto?.name) {
      this.deleteModalTitle = 'Are you sure you want to delete "' + this.mealDto.name + '"?';
    }
    this.isDeleteModalOpen = true;
  }

  handleDeleteModalSubmit(): void {
    this.onDelete();
  }

  openEditModal(isEditName: boolean): void {
    this.isEditModalOpen = true;
    this.isEditModalEditName = isEditName;
  }

  handleEditModalCancel(): void {
    this.setDefaultNumberOfPeopleAndDefaultMealEditDtoValues();
  }

  handleEditModalSubmit(): void {
    this.onEdit(this.isEditModalEditName ? 'Meal name updated.' : 'Meal scaled.');
  }
}
