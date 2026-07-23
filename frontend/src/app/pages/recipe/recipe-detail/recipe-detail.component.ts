import {
  Component,
  OnInit,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { SearchInputComponent } from "../../../components/Input/SearchInput";
import { SimpleButtonComponent } from "../../../components/Button/SimpleButton";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { LoadingSpinnerComponent } from "../../../components/LoadingSpinner/LoadingSpinner";
import { PageLayoutComponent } from "../../../components/Layout/PageLayout";
import { ButtonVariant } from "../../../components/Button/SimpleButton";
import { SimpleModalComponent } from "../../../components/Modal/SimpleModalComponent";
import { FileUploadComponent } from "../../../components/FileUpload/file-upload.component";
import {
  InputFieldComponent,
  InputType,
} from "../../../components/Input/InputField";
import { ToastrService } from "ngx-toastr";
import { ErrorService } from "../../../globals/error.service";
import { IngredientComputationService } from "../../../service/ingredient-computation.service";
import { TokenService } from "../../../security/token.service";
import { AddMealToMenuModalComponent } from "./components/add-meal/add-meal-to-menu-modal.component";
import { MarkdownViewerComponent } from "../../../components/Markdown/MarkdownViewer/markdown-viewer.component";
import {
  IngredientUnitDto,
  MenusApiService,
  RecipeDto,
  RecipesApiService,
} from "../../../../generated";

@Component({
  imports: [
    RouterModule,
    PageLayoutComponent,
    SimpleButtonComponent,
    FormsModule,
    LoadingSpinnerComponent,
    SimpleModalComponent,
    AddMealToMenuModalComponent,
    MarkdownViewerComponent,
  ],
  selector: "app-recipe-detail",
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./recipe-detail.component.html",
})
export class RecipeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private recipesApiService = inject(RecipesApiService);
  private menusApiService = inject(MenusApiService);
  private ingredientComputationService = inject(IngredientComputationService);
  private router = inject(Router);
  private toastr = inject(ToastrService);
  private errorService = inject(ErrorService);
  protected tokenService = inject(TokenService);

  ButtonVariant = ButtonVariant;
  InputType = InputType;
  recipeId: number | null = null;
  recipeDto: RecipeDto | undefined;
  loadingRecipe: boolean = true;
  errorNoRecipeFound: string = "";

  addModalTitle: string = "";
  isAddModalOpen: boolean = false;
  isDeleteModalOpen: boolean = false;
  deleteModalTitle: string = "";

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.recipeId = +params["id"];
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
        this.errorNoRecipeFound = "Recipe not found.";
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
      this.menusApiService
        .addMealToMenu(1, { recipeId: this.recipeId })
        .subscribe({
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
          this.router.navigate(["/recipes"]);
          this.toastr.success("Recipe deleted.");
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
