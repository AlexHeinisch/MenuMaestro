import { Component, OnInit, ViewChild } from '@angular/core';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { SimpleButtonComponent, ButtonVariant } from '../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../../../security/token.service';
import { ErrorService } from '../../../globals/error.service';
import { StringFormattingService } from '../../../service/string-formatting.service';
import { RequestIngredientModalComponent } from '../components/request-ingredient-modal/request-ingredient-modal.component';
import {
  IngredientDto,
  IngredientDtoWithCategory, IngredientListPaginatedDto,
  IngredientsApiService,
  IngredientWithCategoryListPaginatedDto
} from "../../../../generated";

@Component({
  selector: 'app-ingredients-management',
  standalone: true,
  imports: [
    PageLayoutComponent,
    SimpleModalComponent,
    SimpleButtonComponent,
    SearchInputComponent,
    CommonModule,
    RouterModule,
    FormsModule,
    LoadingSpinnerComponent,
    PaginationControlsComponent,
    RequestIngredientModalComponent,
  ],
  templateUrl: './ingredients-management.component.html',
})
export class IngredientsManagementComponent implements OnInit {
  @ViewChild('replacementSearchInput') replacementSearchInput!: SearchInputComponent;
  @ViewChild('ownIngredientSearchInput') ownIngredientSearchInput!: SearchInputComponent;
  @ViewChild('requestIngredientModalComponent') requestIngredientModalComponent!: RequestIngredientModalComponent;

  ButtonVariant = ButtonVariant;
  InputType = InputType;

  requestedIngredients: IngredientWithCategoryListPaginatedDto | undefined;
  isLoadingIngredientRequests: boolean = true;

  isAcceptIngredientModalOpen: boolean = false;
  isReplaceIngredientModalOpen: boolean = false;
  isRejectIngredientModalOpen: boolean = false;

  selectedIngredient: IngredientDtoWithCategory = {} as IngredientDtoWithCategory;

  ingredientsOptions: IngredientDto[] = [];
  ingredientsOptionsNames: string[] = [];
  replacementIngredientId: number = -1;

  ingredientsOptionsOwn: IngredientDto[] = [];
  ingredientsOptionsNamesOwn: string[] = [];

  isRequestIngredientModalOpen: boolean = false;
  requestedIngredientName: string = '';
  requestedIngredientModalTitle: string = 'New Ingredient: ';
  newIngredientBtnText: string = 'Create';

  currentPage = 1;
  pageSize = 5;

  constructor(
    private ingredientsApiService: IngredientsApiService,
    private toastr: ToastrService,
    private tokenService: TokenService,
    private errorService: ErrorService,
    private stringFormattingService: StringFormattingService
  ) {}

  ngOnInit(): void {
    this.fetchIngredientRequests();
  }

  fetchIngredientRequests(requestedPage: number = 1): void {
    this.currentPage = requestedPage;
    this.ingredientsApiService.ingredientSuggestions(this.currentPage - 1, this.pageSize).subscribe({
      next: (ingredientListPaginated) => {
        console.log(ingredientListPaginated);
        this.requestedIngredients = ingredientListPaginated;
        this.isLoadingIngredientRequests = false;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.isLoadingIngredientRequests = false;
      },
    });
  }

  onPageChange(newPage: number): void {
    this.fetchIngredientRequests(newPage);
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  openAcceptIngredientModal(ingredient: IngredientDtoWithCategory): void {
    this.isAcceptIngredientModalOpen = true;
    this.selectedIngredient = ingredient;
  }

  openReplaceIngredientModal(ingredient: IngredientDtoWithCategory): void {
    this.isReplaceIngredientModalOpen = true;
    this.selectedIngredient = ingredient;
  }

  openRejectIngredientModal(ingredient: IngredientDtoWithCategory): void {
    this.isRejectIngredientModalOpen = true;
    this.selectedIngredient = ingredient;
  }

  handleAcceptIngredientModalSubmit(): void {
    this.ingredientsApiService.approveIngredient(this.selectedIngredient.id).subscribe({
      next: (ingredient) => {
        console.log(ingredient);
        this.fetchIngredientRequests();
        this.toastr.success('Ingredient accepted.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  handleReplaceIngredientModalSubmit(): void {
    this.ingredientsApiService
      .replaceIngredient(this.selectedIngredient.id, { ingredientId: this.replacementIngredientId })
      .subscribe({
        next: (ingredient) => {
          console.log(ingredient);
          this.fetchIngredientRequests();
          this.toastr.success('Ingredient replaced.');
          this.replacementSearchInput.resetSearch();
        },
        error: (err) => {
          this.errorService.printErrorResponse(err);
          this.replacementSearchInput.resetSearch();
        },
      });
  }

  handleReplaceIngredientModalCancel(): void {
    this.replacementSearchInput.resetSearch();
  }

  handleRejectIngredientModalSubmit(): void {
    this.ingredientsApiService.deleteIngredient(this.selectedIngredient.id).subscribe({
      next: (ingredient) => {
        console.log(ingredient);
        this.fetchIngredientRequests();
        this.toastr.success('Ingredient rejected.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  formatString(input: string): string | undefined {
    return this.stringFormattingService.formatStringInput(input);
  }

  searchIngredient(searchTerm: string) {
    this.ingredientsApiService.searchIngredients(0, 5, undefined, searchTerm).subscribe({
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

  searchIngredientOwn(searchTerm: string) {
    this.ingredientsApiService.searchIngredients(0, 5, undefined, searchTerm).subscribe({
      next: (response: IngredientListPaginatedDto) => {
        if (response.content) {
          this.ingredientsOptionsOwn = response.content;
          this.ingredientsOptionsNamesOwn = this.ingredientsOptionsOwn.map((ingredient) => ingredient.name);
        } else {
          this.ingredientsOptionsOwn = [];
          this.ingredientsOptionsNamesOwn = [];
        }
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onIngredientSelected(selected: string) {
    const selectedReplacementIngredient = this.ingredientsOptions.find((ingredient) => ingredient.name === selected);

    if (selectedReplacementIngredient) {
      this.replacementIngredientId = selectedReplacementIngredient.id;
    } else {
      this.replacementIngredientId = -1;
    }
  }

  isFormValid(): boolean {
    return this.replacementIngredientId !== -1;
  }

  onIngredientSelectedOwn(selected: string) {
    if (selected !== '') {
      this.toastr.error('There already exists an ingredient named: ' + selected + '.');
      this.ownIngredientSearchInput.resetSearch();
    }
  }

  onRequestIngredientSelected(selected: string) {
    this.isRequestIngredientModalOpen = true;
    this.requestedIngredientName = selected;
    this.requestedIngredientModalTitle = 'New Ingredient: ' + '"' + this.requestedIngredientName + '"';
  }

  handleRequestedIngredientModalSubmit(): void {
    this.requestIngredientModalComponent.suggestIngredient().subscribe({
      next: (ingredient) => {
        this.toastr.success('Ingredient created.');
        this.ownIngredientSearchInput.resetSearch();
      },
      error: (err) => {
        this.ownIngredientSearchInput.resetSearch();
        this.errorService.printErrorResponse(err);
      },
    });
  }

  handleRequestedIngredientModalCancel(): void {
    this.ownIngredientSearchInput.resetSearch();
  }
}
