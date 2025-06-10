import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { WidePageLayoutComponent } from '../../../components/Layout/WidePageLayout';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { RouterModule } from '@angular/router';
import { RecipesApiService } from '../../../../generated/recipes/api/recipes.service';
import { CookingAppliancesApiService } from '../../../../generated/cooking-appliances/api/cooking-appliances.service';
import { FormsModule } from '@angular/forms';
import { RecipeDto, RecipeListPaginatedDto, RecipeVisibility } from '../../../../generated/recipes/model/models';
import { CookingApplianceDto } from '../../../../generated/cooking-appliances/model/models';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { IngredientsApiService } from '../../../../generated/ingredients/api/ingredients.service';
import { IngredientDto } from '../../../../generated/ingredients/model/models';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { ErrorService } from '../../../globals/error.service';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';

// Define the types for filter options and filters
interface FilterOption {
  value: string;
  label: string;
  checked: boolean;
}

interface Filter {
  id: string;
  name: string;
  isExpanded: boolean;
  options: FilterOption[];
}

@Component({
  standalone: true,
  imports: [
    RouterModule,
    CommonModule,
    WidePageLayoutComponent,
    SearchInputComponent,
    SimpleButtonComponent,
    FormsModule,
    InputFieldComponent,
    LoadingSpinnerComponent,
    PaginationControlsComponent,
  ],
  selector: 'app-recipes-overview',
  templateUrl: './recipes-overview.component.html',
})
export class RecipesOverviewComponent implements OnInit {
  // Button variant
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  // State
  isMobileFilterOpen = false;
  sortMenuOpen = false;
  recipeList: RecipeDto[] | undefined = undefined;
  recipeListPaginated: RecipeListPaginatedDto | undefined = undefined;
  isLoading: boolean = true;
  recipeService: RecipesApiService = inject(RecipesApiService);
  cookingAppliancesService: CookingAppliancesApiService = inject(CookingAppliancesApiService);
  ingredientsService: IngredientsApiService = inject(IngredientsApiService);
  searchTerm: string = '';
  authorSearchTerm: string = '';
  descriptionSearchTerm: string = '';

  ingredientSearchTerm: string = '';
  ingredientsOptions: IngredientDto[] = [];
  ingredientsSelected: IngredientDto[] = [];

  // Pagination
  currentPage: number = 0;
  pageSize: number = 12;

  // Filters
  visibilityOptions: string[] = Object.keys(RecipeVisibility)
    .filter((key) => isNaN(Number(key)))
    .map((key) => RecipeVisibility[key as keyof typeof RecipeVisibility]);
  selectedVisibility = RecipeVisibility.Public;

  filters: Filter[] = [
    {
      id: 'cooking-appliance',
      name: 'Cooking Appliances',
      isExpanded: false,
      options: [],
    },
  ];
  cookingAppliances: CookingApplianceDto[] = [];
  recipeCookingApplianceSearchDtos: number[] = [];

  constructor(private errorService: ErrorService) {}

  ngOnInit(): void {
    // Construct cooking appliances filter
    this.cookingAppliancesService.getCookingAppliances(undefined, undefined, undefined, '').subscribe({
      next: (response) => {
        this.cookingAppliances = response.content || [];
        // Dynamically add cooking appliances options from the fetched appliances
        this.filters = this.filters.map((filter) => {
          if (filter.id === 'cooking-appliance') {
            filter.options = this.cookingAppliances.map((appliance) => {
              const name = appliance.name || 'Unknown Cooking Appliance'; // Use fallback value for undefined name
              return {
                value: name.toLowerCase().replace(/\s+/g, '-'),
                label: name,
                checked: false,
              };
            });
          }
          return filter;
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorService.printErrorResponse(err);
      },
    });

    this.fetchRecipes();
  }

  fetchRecipes(requestedPage: number = 1): void {
    this.currentPage = requestedPage;
    // Filter out the checked cooking appliances options
    const checkedCookingAppliances =
      this.filters
        .find((filter) => filter.id === 'cooking-appliance')
        ?.options.filter((option) => option.checked)
        .map((option) => {
          // Find the cooking appliance id from your list of appliances
          const appliance = this.cookingAppliances.find((appliance) => {
            const applianceName = appliance?.name?.toLowerCase().replace(/\s+/g, '-') ?? '';
            return applianceName === option.value;
          });
          return appliance
            ? {
                id: appliance.id,
                max: 1,
              }
            : null;
        })
        .filter((option) => option !== null) || []; // Remove null values (in case an appliance wasn't found)

    // Generate the cooking appliance search dto from the checked filters
    const ingredientIds = this.ingredientsSelected.map((ingredient) => ingredient.id).filter((id) => id !== undefined);
    this.recipeCookingApplianceSearchDtos = checkedCookingAppliances.map((item) => item.id);

    this.recipeService
      .getRecipes(
        this.currentPage - 1,
        this.pageSize,
        this.searchTerm,
        this.descriptionSearchTerm,
        this.authorSearchTerm,
        ingredientIds,
        this.recipeCookingApplianceSearchDtos,
        this.selectedVisibility
      )
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          this.recipeListPaginated = response;
          this.recipeList = response.content || []; // Use content property for recipes
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
        },
      });
  }

  toggleSortMenu() {
    this.sortMenuOpen = !this.sortMenuOpen;
  }

  toggleMobileFilter(): void {
    this.isMobileFilterOpen = !this.isMobileFilterOpen;
  }

  toggleSection(index: number): void {
    this.filters[index].isExpanded = !this.filters[index].isExpanded;
  }

  onSearch(searchTerm: string): void {
    this.searchTerm = searchTerm;
    this.fetchRecipes();
  }

  onDescriptionSearch(searchTerm: any): void {
    this.descriptionSearchTerm = searchTerm;
    this.fetchRecipes();
  }

  onAuthorSearch(searchTerm: any): void {
    this.authorSearchTerm = searchTerm;
    this.fetchRecipes();
  }

  onIngredientSearch(searchTerm: any): void {
    this.ingredientSearchTerm = searchTerm;
    // Limit suggestions to 5
    this.ingredientsService.searchIngredients(0, 5, undefined, searchTerm).subscribe({
      next: (response) => {
        this.ingredientsOptions = response.content || [];
      },
      error: (err) => {
        this.isLoading = false;
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onIngredientSearchSelect(selectedIngredient: string): void {
    const ingredient: IngredientDto | undefined = this.ingredientsOptions.find(
      (ingredient) => ingredient.name === selectedIngredient
    );
    if (ingredient !== undefined) {
      // Check if the ingredient is already in the list
      const exists = this.ingredientsSelected.some((existingIngredient) => existingIngredient.id === ingredient.id);

      if (!exists) {
        this.ingredientsSelected.push(ingredient);
      } else {
        this.errorService.printErrorResponse('Ingredient already exists in the selection');
      }
    } else {
      this.errorService.printErrorResponse('Ingredient is undefined and cannot be added');
    }
    this.ingredientsOptions = [];
    this.ingredientSearchTerm = '';
    this.fetchRecipes();
  }

  removeIngredient(ingredient: any): void {
    this.ingredientsSelected.splice(ingredient, 1);
    this.fetchRecipes();
  }

  get ingredientSearchSuggestions(): string[] {
    return this.ingredientsOptions
      .map((ingredient) => ingredient.name)
      .filter((name): name is string => name !== undefined);
  }

  onFilterChange(filterIndex: number, optionIndex: number): void {
    // Toggle the 'checked' state of the filter option
    this.filters[filterIndex].options[optionIndex].checked = !this.filters[filterIndex].options[optionIndex].checked;
    this.fetchRecipes();
  }

  onPageChange(newPage: number): void {
    this.fetchRecipes(newPage);
    // Scroll to the top
    window.scrollTo({
      top: 0,
      behavior: 'smooth', // Optional: makes the scroll smooth
    });
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = 'default-recipe.png';
  }
}
