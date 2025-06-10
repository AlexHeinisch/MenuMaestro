import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { RecipesApiService } from '../../../../generated/recipes/api/recipes.service';
import { IngredientUnitDto, RecipeDto } from '../../../../generated/recipes/model/models';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant } from '../../../components/Button/SimpleButton';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { FileUploadComponent } from '../../../components/FileUpload/file-upload.component';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { MenusApiService } from '../../../../generated/menus';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../globals/error.service';
import { IngredientComputationService } from '../../../service/ingredient-computation.service';
import { TokenService } from '../../../security/token.service';
import { AddMealToMenuModalComponent } from './components/add-meal/add-meal-to-menu-modal.component';

@Component({
  standalone: true,
  imports: [
    RouterModule,
    CommonModule,
    PageLayoutComponent,
    SearchInputComponent,
    SimpleButtonComponent,
    FormsModule,
    LoadingSpinnerComponent,
    SimpleModalComponent,
    FileUploadComponent,
    InputFieldComponent,
    AddMealToMenuModalComponent,
  ],
  selector: 'app-recipe-detail',
  templateUrl: './recipe-detail.component.html',
})
export class RecipeDetailComponent implements OnInit {
  ButtonVariant = ButtonVariant;
  InputType = InputType;
  recipeId: number | null = null;
  recipeDto: RecipeDto | undefined;
  loadingRecipe: boolean = true;
  errorNoRecipeFound: string = '';

  addModalTitle: string = '';
  isAddModalOpen: boolean = false;
  isDeleteModalOpen: boolean = false;
  deleteModalTitle: string = '';

  constructor(
    private route: ActivatedRoute,
    private recipesApiService: RecipesApiService,
    private menusApiService: MenusApiService,
    private ingredientComputationService: IngredientComputationService,
    private router: Router,
    private toastr: ToastrService,
    private errorService: ErrorService,
    protected tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.recipeId = +params['id'];
      this.fetchRecipe(this.recipeId);
    });
  }

  fetchRecipe(recipeId: number): void {
    this.recipesApiService.getRecipeById(recipeId).subscribe({
      next: (recipe: RecipeDto) => {
        this.loadingRecipe = false;
        this.recipeDto = recipe;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.loadingRecipe = false;
        this.errorNoRecipeFound = 'Recipe not found.';
      },
    });
  }

  openAddModal(): void {
    if (this.recipeDto?.name) {
      this.addModalTitle = `Add this recipe to an organization's menu`;
    }
    this.isAddModalOpen = true;
  }

  handleAddModalSubmit(): void {
    if (this.recipeId !== null) {
      this.menusApiService.addMealToMenu(1, { recipeId: this.recipeId }).subscribe({
        next: () => {
          this.isAddModalOpen = false;
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  onEdit(): void {
    this.router.navigate([`/recipes/${this.recipeId}/edit`]);
  }

  onDelete(): void {
    if (this.recipeId !== null) {
      this.recipesApiService.deleteRecipeById(this.recipeId).subscribe({
        next: () => {
          this.router.navigate(['/recipes']);
          this.toastr.success('Recipe deleted.');
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
    }
  }

  openDeleteModal(): void {
    if (this.recipeDto?.name) {
      this.deleteModalTitle = `Are you sure you want to delete "${this.recipeDto.name}"?`;
    }
    this.isDeleteModalOpen = true;
  }

  prepareUnit(unit: IngredientUnitDto): string {
    return this.ingredientComputationService.formatUnitDisplay(unit);
  }

  isAuthor(): boolean {
    return this.recipeDto?.author === this.tokenService.getUsername();
  }
}
