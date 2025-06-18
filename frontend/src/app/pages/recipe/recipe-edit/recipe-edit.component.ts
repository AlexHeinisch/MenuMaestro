import { Component, OnInit, QueryList, ViewChild, ViewChildren, EventEmitter, Input, Output } from '@angular/core';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ActivatedRoute, Router } from '@angular/router';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { InfoMessageComponent, InfoMessageType } from '../../../components/Card/InfoMessage';
import { FileUploadComponent } from '../../../components/FileUpload/file-upload.component';
import { TokenService } from '../../../security/token.service';
import { ErrorService } from '../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { RequestIngredientModalComponent } from '../../ingredient/components/request-ingredient-modal/request-ingredient-modal.component';
import { Observable } from 'rxjs';
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
  RecipeDto,
  RecipesApiService,
  RecipeVisibility
} from "../../../../generated";

@Component({
  selector: 'app-recipe-edit',
  standalone: true,
  imports: [
    SimpleButtonComponent,
    InputFieldComponent,
    SearchInputComponent,
    PageLayoutComponent,
    LoadingSpinnerComponent,
    InfoMessageComponent,
    FormsModule,
    CommonModule,
    FileUploadComponent,
    RequestIngredientModalComponent,
    SimpleModalComponent,
  ],
  templateUrl: './recipe-edit.component.html',
})
export class EditRecipeComponent implements OnInit {
  @ViewChild('requestIngredientModalComponent') requestIngredientModalComponent!: RequestIngredientModalComponent;
  @ViewChildren('ingredientSearchInput') searchInputs!: QueryList<SearchInputComponent>;

  InputType = InputType;
  ButtonVariant = ButtonVariant;
  InfoMessageType = InfoMessageType;

  recipeId!: number;
  recipeEdit: RecipeCreateEditDto = {
    name: '',
    servings: 1,
    ingredients: [],
    cookingAppliances: [],
    description: '',
    author: '',
    visibility: RecipeVisibility.Public,
  };

  cookingAppList: { name: string; amount: number | null; id: number | null }[] = [{ name: '', amount: null, id: null }];
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

  loadingRecipe: boolean = true;
  errorNoRecipeFound: string = '';

  initialImageLink: string | undefined = undefined;

  @Input() fetchRecipeValueHandler: () => Observable<RecipeDto> = () => null!;
  @Input() redirectPath: string = '';
  @Input() editRecipeHandler: (recipe: RecipeCreateEditDto) => Observable<any> = () => null!;
  @Input() title: string = 'Edit Recipe';
  @Input() hideVisibilityNameAndServings: boolean = false;

  constructor(
    private recipesApiService: RecipesApiService,
    private ingredientsApiService: IngredientsApiService,
    private cookingAppServiceApi: CookingAppliancesApiService,
    private tokenService: TokenService,
    private route: ActivatedRoute,
    private router: Router,
    private errorService: ErrorService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.recipeId = +params['id'];
      this.fetchRecipe();
    });
  }

  fetchRecipe(): void {
    this.fetchRecipeValueHandler().subscribe({
      next: (recipe) => {
        this.loadingRecipe = false;
        this.recipeEdit = {
          name: recipe.name,
          servings: recipe.servings,
          description: recipe.description,
          author: recipe.author,
          visibility: recipe.visibility,
          ingredients: [],
          cookingAppliances: [],
          imageId: recipe.imageId,
        };
        this.initialImageLink = recipe.imageLink;

        this.ingredientsList = recipe.ingredients?.map((ing) => ({
          name: ing.name,
          amount: ing.amount,
          unit: ing.unit,
          id: ing.id,
        })) as { name: string; amount: number | null; unit: IngredientUnitDto | null; id: number | null }[];

        this.cookingAppList = recipe.cookingAppliances?.map((ca) => ({
          name: ca.name,
          amount: ca.amount,
          id: ca.id,
        })) as { name: string; amount: number | null; id: number | null }[];
      },
      error: (err) => {
        this.errorNoRecipeFound = 'No recipe with the given id exists.';
        this.loadingRecipe = false;
        this.errorService.printErrorResponse(err);
      },
    });
  }

  handleImageUploaded(uploaded: ImageUploadResponseDto) {
    this.recipeEdit.imageId = uploaded.identifier;
  }

  handleImageRemoved(removed: boolean) {
    this.recipeEdit.imageId = undefined;
  }

  onInitialImageRemoved() {
    this.initialImageLink = undefined;
    this.recipeEdit.imageId = undefined;
  }

  editRecipe() {
    const loggedInUser = this.tokenService.getUsername();
    if (loggedInUser) {
      this.recipeEdit.author = loggedInUser;
    } else {
      this.router.navigate([`/login`]);
    }
    this.editRecipeHandler(this.recipeEdit).subscribe({
      next: (response) => {
        this.router.navigate([this.redirectPath]);
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
    const selectedIngredient = this.ingredientsOptions?.find((ingredient) => ingredient.name === selected);

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

  onSubmit(form: NgForm) {
    if (form.valid) {
      this.recipeEdit.ingredients = this.ingredientsList
        .filter((ingredient) => ingredient.name || ingredient.amount !== null || ingredient.unit !== null)
        .map((ingredient) => {
          return {
            id: ingredient.id,
            amount: ingredient.amount,
            unit: ingredient.unit,
          } as IngredientUseCreateEditDto;
        });

      this.recipeEdit.cookingAppliances = this.cookingAppList
        .filter((cookingApp) => cookingApp.name || cookingApp.amount !== null)
        .map((cookingApp) => {
          return {
            id: cookingApp.id,
            amount: cookingApp.amount,
          } as CookingApplianceUseCreateEditDto;
        });
      this.editRecipe();
    } else {
      this.errorService.printErrorResponse('Form is invalid');
    }
  }

  cancelClicked() {
    this.router.navigate([this.redirectPath]);
  }

  onRequestIngredientSelected(selected: string, index: number) {
    this.isRequestIngredientModalOpen = true;
    this.requestedIngredientName = selected;
    this.requestedIngredientModalTitle = 'New Ingredient: ' + '"' + this.requestedIngredientName + '"';
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
}
