import { Component, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { Router } from '@angular/router';
import { FileUploadComponent } from '../../../components/FileUpload/file-upload.component';
import { TokenService } from '../../../security/token.service';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../globals/error.service';
import { RequestIngredientModalComponent } from '../../ingredient/components/request-ingredient-modal/request-ingredient-modal.component';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import {
  CookingApplianceDto,
  CookingApplianceListPaginatedDto,
  CookingAppliancesApiService,
  CookingApplianceUseCreateEditDto,
  ImageUploadResponseDto,
  IngredientDto,
  IngredientListPaginatedDto,
  IngredientsApiService,
  IngredientUnitDto,
  IngredientUseCreateEditDto,
  RecipeCreateEditDto,
  RecipesApiService,
  RecipeVisibility
} from "../../../../generated";

@Component({
    selector: 'app-recipe-create',
    imports: [
        SimpleButtonComponent,
        InputFieldComponent,
        SearchInputComponent,
        PageLayoutComponent,
        FormsModule,
        CommonModule,
        FileUploadComponent,
        RequestIngredientModalComponent,
        SimpleModalComponent,
    ],
    templateUrl: './recipe-create.component.html'
})
export class CreateRecipeComponent {
  @ViewChild('requestIngredientModalComponent') requestIngredientModalComponent!: RequestIngredientModalComponent;
  @ViewChildren('ingredientSearchInput') searchInputs!: QueryList<SearchInputComponent>;

  InputType = InputType;
  ButtonVariant = ButtonVariant;

  recipeCreate: RecipeCreateEditDto = {
    name: '',
    servings: 1,
    ingredients: [],
    cookingAppliances: [],
    description: '',
    author: '',
    visibility: RecipeVisibility.Public,
  };

  cookingAppList: { name: string; amount: number | null; id: number | null }[] = [];
  ingredientsList: { name: string; amount: number | null; unit: IngredientUnitDto | null; id: number | null }[] = [
    { name: '', amount: null, unit: null, id: null },
  ];

  measurementUnits = Object.values(IngredientUnitDto);
  visibilityTypes = Object.values(RecipeVisibility);

  cookingAppOptions: CookingApplianceDto[] = [];
  cookingAppOptionsNames: string[] = [];

  ingredientsOptions: IngredientDto[] = [];
  ingredientsOptionsNames: string[] = [];

  isRequestIngredientModalOpen: boolean = false;
  requestedIngredientName: string = '';
  requestedIngredientModalTitle: string = 'New Ingredient: ';
  selectedIndexForRequest: number = -1;
  newIngredientBtnText: string = 'Request';

  constructor(
    private recipesApiService: RecipesApiService,
    private ingredientsApiService: IngredientsApiService,
    private cookingAppServiceApi: CookingAppliancesApiService,
    private tokenService: TokenService,
    private router: Router,
    private toastr: ToastrService,
    private errorService: ErrorService
  ) {}

  createRecipe() {
    const loggedInUser = this.tokenService.getUsername();
    if (loggedInUser) {
      this.recipeCreate.author = loggedInUser;
    } else {
      this.router.navigate([`/login`]);
    }
    this.recipesApiService.createRecipe(this.recipeCreate).subscribe({
      next: (response) => {
        this.router.navigate([`/recipes/${response.id}`]);
        this.toastr.success('Recipe created.');
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  addCookingApp() {
    this.cookingAppList.push({ name: '', amount: null, id: null });
  }

  removeCookingApp(index: number) {
    this.cookingAppList.splice(index, 1);
  }

  addIngredient() {
    this.ingredientsList.push({ name: '', amount: null, unit: null, id: null });
  }

  removeIngredient(index: number) {
    this.ingredientsList.splice(index, 1);
  }

  searchIngredient(searchTerm: string) {
    this.ingredientsApiService.searchIngredients(0, 5, undefined, searchTerm).subscribe({
      next: (response: IngredientListPaginatedDto) => {
        if (response.content) {
          this.ingredientsOptions = response.content;
          this.ingredientsOptionsNames = this.ingredientsOptions.map((ingredient) => ingredient.name!);
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

  onIngredientSelected(selected: string, index: number) {
    const selectedIngredient = this.ingredientsOptions.find((ingredient) => ingredient.name === selected);

    if (selectedIngredient) {
      if (selectedIngredient.defaultUnit) {
        this.ingredientsList[index].unit = selectedIngredient.defaultUnit;
      }
      if (selectedIngredient.id) {
        this.ingredientsList[index].id = selectedIngredient.id;
      }
      if (selectedIngredient.name) {
        this.ingredientsList[index].name = selectedIngredient.name;
      }
      this.ingredientsList[index].amount = 1;
    } else {
      this.ingredientsList[index].unit = null;
      this.ingredientsList[index].id = null;
      this.ingredientsList[index].amount = null;
      this.ingredientsList[index].name = '';
    }
  }

  searchCookingApp(searchTerm: string) {
    this.cookingAppServiceApi.getCookingAppliances(0, 5, undefined, searchTerm).subscribe({
      next: (response: CookingApplianceListPaginatedDto) => {
        if (response.content) {
          this.cookingAppOptions = response.content;
          this.cookingAppOptionsNames = this.cookingAppOptions.map((cookingApp) => cookingApp.name!);
        } else {
          this.cookingAppOptions = [];
          this.cookingAppOptionsNames = [];
        }
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onCookingAppSelected(selected: string, index: number) {
    const selectedCookingApp = this.cookingAppOptions.find((cookingApp) => cookingApp.name === selected);

    if (selectedCookingApp) {
      if (selectedCookingApp.name != null) {
        this.cookingAppList[index].name = selectedCookingApp.name;
      }
      if (selectedCookingApp.id) {
        this.cookingAppList[index].id = selectedCookingApp.id;
      }
      this.cookingAppList[index].amount = 1;
    } else {
      this.cookingAppList[index].id = null;
      this.cookingAppList[index].amount = null;
      this.cookingAppList[index].name = '';
    }
  }

  handleImageUploaded(uploaded: ImageUploadResponseDto) {
    this.recipeCreate.imageId = uploaded.identifier;
  }

  handleImageRemoved(removed: boolean) {
    this.recipeCreate.imageId = undefined;
  }

  onSubmit(form: NgForm) {
    if (form.valid) {
      this.recipeCreate.ingredients = this.ingredientsList
        .filter((ingredient) => ingredient.name || ingredient.amount !== null || ingredient.unit !== null)
        .map((ingredient) => {
          return {
            id: ingredient.id,
            amount: ingredient.amount,
            unit: ingredient.unit,
          } as IngredientUseCreateEditDto;
        });

      this.recipeCreate.cookingAppliances = this.cookingAppList
        .filter((cookingApp) => cookingApp.name || cookingApp.amount !== null)
        .map((cookingApp) => {
          return {
            id: cookingApp.id,
            amount: cookingApp.amount,
          } as CookingApplianceUseCreateEditDto;
        });
      this.createRecipe();
    } else {
      console.error('Form is invalid');
    }
  }

  onRequestIngredientSelected(selected: string, index: number) {
    this.isRequestIngredientModalOpen = true;
    this.requestedIngredientName = selected;
    this.requestedIngredientModalTitle = 'New Ingredient: ' + '"' + this.requestedIngredientName + '"';
    if (this.tokenService.isAdmin()) {
      this.newIngredientBtnText = 'Create';
    }
    this.selectedIndexForRequest = index;
  }

  handleRequestedIngredientModalSubmit(): void {
    this.requestIngredientModalComponent.suggestIngredient().subscribe({
      next: (ingredient) => {
        this.ingredientsList[this.selectedIndexForRequest].unit = ingredient.defaultUnit;
        this.ingredientsList[this.selectedIndexForRequest].id = ingredient.id;
        this.ingredientsList[this.selectedIndexForRequest].amount = 1;
        this.ingredientsList[this.selectedIndexForRequest].name = this.requestedIngredientName;
      },
      error: (err) => {
        this.ingredientsList[this.selectedIndexForRequest].name = '';
        this.searchInputs.toArray()[this.selectedIndexForRequest].resetSearch();
        this.errorService.printErrorResponse(err);
      },
    });
  }

  handleRequestedIngredientModalCancel(): void {
    this.searchInputs.toArray()[this.selectedIndexForRequest].resetSearch();
  }

  onCancel() {
    this.router.navigate(['/recipes']);
  }
}
