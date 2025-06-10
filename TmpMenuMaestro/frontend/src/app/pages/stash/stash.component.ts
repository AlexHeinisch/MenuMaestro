import { Component, OnInit } from '@angular/core';
import { PageLayoutComponent } from '../../components/Layout/PageLayout';
import { LoadingSpinnerComponent } from '../../components/LoadingSpinner/LoadingSpinner';
import { NgForOf, NgIf } from '@angular/common';
import {
  IngredientUseCreateEditDto,
  IngredientUseDto,
  StashApiService,
  StashResponseDto,
  StashSearchResponseDto,
} from '../../../generated/stash';
import { ActivatedRoute } from '@angular/router';
import { ButtonVariant, SimpleButtonComponent } from '../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import {
  IngredientDto,
  IngredientListPaginatedDto,
  IngredientsApiService,
  IngredientUnitDto,
} from '../../../generated/ingredients';
import { ComplexModalComponent } from '../../components/Modal/ComplexModalComponent';
import { SimpleModalComponent } from '../../components/Modal/SimpleModalComponent';
import { SearchInputComponent } from '../../components/Input/SearchInput';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../globals/error.service';
import { IngredientComputationService } from '../../service/ingredient-computation.service';
import { TokenService } from '../../security/token.service';

@Component({
  selector: 'app-stash',
  standalone: true,
  imports: [
    PageLayoutComponent,
    LoadingSpinnerComponent,
    NgIf,
    NgForOf,
    SimpleButtonComponent,
    InputFieldComponent,
    FormsModule,
    ComplexModalComponent,
    SimpleModalComponent,
    SearchInputComponent,
  ],
  templateUrl: './stash.component.html',
})
export class StashComponent implements OnInit {
  measurementUnits = Object.values(IngredientUnitDto);

  stashId: number = -1;
  loadingError: boolean = false;

  stash: StashResponseDto | undefined;
  checkableEntries: { checked: boolean; ingredient: IngredientUseDto }[] | undefined;
  editStashOriginalEntry: IngredientUseDto | null = null;
  editStashEntry: IngredientUseDto | null = null;

  ingredientsOptions: IngredientDto[] = [];
  ingredientsOptionsNames: string[] = [];
  ingredientToAdd: IngredientUseDto | null = null;
  ingredientToAddSelected: boolean = false;
  bulkMoveModalOpen: boolean = false;
  bulkDeleteModalOpen: boolean = false;
  selectAll: boolean = false;

  deleteModalIngredient: IngredientUseDto | null = null;

  moveStashSearchTerm: string = '';
  stashSearchOptions: [number, string][] = [];
  moveToStashId: number | null = null;

  constructor(
    private stashApiService: StashApiService,
    private ingredientsApiService: IngredientsApiService,
    private ingredientComputationService: IngredientComputationService,
    private route: ActivatedRoute,
    private toastr: ToastrService,
    private errorService: ErrorService,
    protected tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.stashId = +params['id'];
    });
    this.loadStash();
  }

  private loadStash(onFinished: (() => void) | null = null) {
    this.stashApiService.getStash(this.stashId).subscribe({
      next: (stashData) => {
        this.stash = stashData;
        this.checkableEntries = stashData.ingredients.map((ingredient) => {
          return { checked: false, ingredient: ingredient };
        });
        this.checkableEntries.sort((a, b) => a.ingredient.name.localeCompare(b.ingredient.name));
        if (onFinished !== null) {
          onFinished();
        }
      },
      error: (error) => {
        this.loadingError = true;
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onEditModalShow(event: any): void {
    if (!event) {
      this.editStashOriginalEntry = null;
      this.editStashEntry = null;
    }
  }

  openEditModal(entry: IngredientUseDto): void {
    this.editStashOriginalEntry = entry;
    this.editStashEntry = Object.assign({}, entry);
  }

  resetModals(): void {
    this.editStashOriginalEntry = null;
    this.editStashEntry = null;
    this.ingredientsOptions = [];
    this.ingredientsOptionsNames = [];
    this.ingredientToAddSelected = false;
    this.moveStashSearchTerm = '';
    this.stashSearchOptions = [];
    this.moveToStashId = null;
    this.deleteModalIngredient = null;
  }

  onEditRemoveIngredient(): void {
    this.editStashEntry!.amount = 0;
    this.onEditSubmit();
  }

  onEditSubmit(): void {
    const entryToUpdate = this.editStashOriginalEntry!;
    const entryUpdate = this.editStashEntry!;
    if (this.editStashEntry!.amount === 0) {
      this.stashApiService
        .updateStashIngredients(this.stashId, [this.editStashEntry!], this.getStashEtag(), 'response')
        .subscribe({
          next: (response) => {
            this.stash!.versionNumber = response.headers.get('ETag')!;
            this.checkableEntries = this.checkableEntries!.filter(
              (i) => i.ingredient.id !== entryToUpdate.id || i.ingredient.unit !== entryToUpdate.unit
            );
          },
          error: (err) => this.handleStashUpdateError(err),
        });
    } else if (this.editStashOriginalEntry!.unit === this.editStashEntry!.unit) {
      // send single update
      this.stashApiService
        .updateStashIngredients(this.stashId, [this.editStashEntry!], this.getStashEtag(), 'response')
        .subscribe({
          next: (response) => {
            this.stash!.versionNumber = response.headers.get('ETag')!;
            Object.assign(entryToUpdate, entryUpdate);
          },
          error: (err) => this.handleStashUpdateError(err),
        });
    } else {
      const oldEntryRemove: IngredientUseCreateEditDto = {
        id: entryToUpdate.id,
        unit: entryToUpdate.unit,
        amount: 0,
      };
      const stashEntryWithSameIngredientAndUnit = this.checkableEntries!.filter(
        (entry) => entry.ingredient.id === entryToUpdate.id && entry.ingredient.unit === entryUpdate.unit
      );
      if (stashEntryWithSameIngredientAndUnit.length > 0) {
        // need to sum amounts
        const other = stashEntryWithSameIngredientAndUnit[0];
        this.editStashEntry!.amount += other.ingredient.amount;
        // remove the other entry
        this.checkableEntries = this.checkableEntries!.filter(
          (i) => i.ingredient.id !== entryUpdate.id || i.ingredient.unit !== entryUpdate.unit
        );
      }
      this.stashApiService
        .updateStashIngredients(this.stashId, [this.editStashEntry!, oldEntryRemove], this.getStashEtag(), 'response')
        .subscribe({
          next: (response) => {
            this.stash!.versionNumber = response.headers.get('ETag')!;
            Object.assign(entryToUpdate, entryUpdate);
          },
          error: (err) => this.handleStashUpdateError(err),
        });
    }
    this.onEditModalShow(false);
  }

  setShowAddIngredientModal(event: any): void {
    if (!event) {
      this.ingredientToAdd = null;
    } else {
      this.ingredientToAdd = {
        name: '',
        unit: IngredientUnitDto.Grams,
        amount: 1,
        id: -1,
      };
    }
  }

  private getStashEtag() {
    return '"' + this.stash?.versionNumber + '"';
  }

  get selectionNotEmpty(): boolean {
    return this.checkableEntries !== undefined && this.checkableEntries.some((e) => e.checked);
  }

  formatStringInput(stringInput: string | undefined): string {
    if (!stringInput) return 'Unknown Status';

    return stringInput
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\band\b/g, '&')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
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

  onIngredientSelectedToAdd(selected: string) {
    const selectedIngredient = this.ingredientsOptions.find((ingredient) => ingredient.name === selected);
    if (selectedIngredient) {
      this.ingredientToAdd = {
        id: selectedIngredient.id,
        name: selectedIngredient.name,
        amount: 1,
        unit: selectedIngredient.defaultUnit,
      };
      this.ingredientToAddSelected = true;
    } else {
      this.ingredientToAdd!.unit = null as unknown as IngredientUnitDto;
      this.ingredientToAdd!.id = -1;
      this.ingredientToAdd!.name = '';
      this.ingredientToAddSelected = false;
    }
  }

  onAddIngredientSubmit(): void {
    if (this.ingredientToAddSelected) {
      const ingredientToAdd = this.ingredientToAdd!;
      const stashEntryWithSameIngredientAndUnit = this.checkableEntries!.filter(
        (entry) => entry.ingredient.id === ingredientToAdd.id && entry.ingredient.unit === ingredientToAdd.unit
      );
      if (stashEntryWithSameIngredientAndUnit.length > 0) {
        // need to sum amounts
        const other = stashEntryWithSameIngredientAndUnit[0];
        ingredientToAdd.amount += other.ingredient.amount;
        other.ingredient.amount = ingredientToAdd.amount;
      }
      this.stashApiService
        .updateStashIngredients(this.stashId, [ingredientToAdd], this.getStashEtag(), 'response')
        .subscribe({
          next: (response) => {
            this.stash!.versionNumber = response.headers.get('ETag')!;
            if (stashEntryWithSameIngredientAndUnit.length === 0) {
              this.checkableEntries!.push({ checked: this.selectAll, ingredient: ingredientToAdd });
              this.checkableEntries!.sort((a, b) => a.ingredient.name.localeCompare(b.ingredient.name));
            }
          },
          error: (err) => this.handleStashUpdateError(err),
        });
    }
  }

  onSelectAll(event: boolean) {
    for (let entry of this.checkableEntries!) {
      entry.checked = event;
    }
  }

  onSelectionChange() {
    const someUnchecked = this.checkableEntries!.some((e) => !e.checked);
    if (this.selectAll && someUnchecked) {
      this.selectAll = false;
    } else if (!this.selectAll && !someUnchecked) {
      this.selectAll = true;
    }
  }

  selectedEntriesCount(): number {
    return this.checkableEntries?.filter((e) => e.checked).length || 0;
  }

  handleStashUpdateError(error: any) {
    // if-match: precondition failed
    if (error.status === 412) {
      this.loadStash(() =>
        this.toastr.error('Stash was changed by someone else. Please review the changes and try again', 'Error')
      );
    } else {
      this.errorService.printErrorResponse(error);
    }
  }

  handleStashSearch(searchTerm: string) {
    this.moveStashSearchTerm = searchTerm;
    if (!searchTerm) {
      this.moveToStashId = null;
    }
    this.stashApiService.searchStashes(this.moveStashSearchTerm, 0, 15).subscribe({
      next: (response) => {
        this.stashSearchOptions = response.map((s) => [s.id, s.name]);
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onStashSelectedForMoving(selectedStashId: number) {
    this.moveToStashId = selectedStashId;
  }

  onMoveIngredientsSubmit() {
    const otherStashId = this.moveToStashId!;
    this.moveToStashId = null;
    this.moveStashSearchTerm = '';
    this.moveToStashId = null;
    const entriesToMove = this.checkableEntries!.filter((e) => e.checked).map((entry) => entry.ingredient);
    this.stashApiService.moveStashIngredients(this.stashId, otherStashId, entriesToMove).subscribe({
      next: (response) => {
        this.loadStash();
        this.toastr.success('The selected ingredients were moved to the selected stash.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onBulkDeleteSelected() {
    const entriesToDelete = this.checkableEntries!.filter((e) => e.checked).map((entry) => entry.ingredient);
    entriesToDelete.forEach((entry) => (entry.amount = 0));
    this.stashApiService
      .updateStashIngredients(this.stashId, entriesToDelete, this.getStashEtag(), 'response')
      .subscribe({
        next: (response) => {
          this.stash!.versionNumber = response.headers.get('ETag')!;
          this.checkableEntries! = this.checkableEntries!.filter((e) => !e.checked);
        },
        error: (err) => this.handleStashUpdateError(err),
      });
  }

  onDeleteSingleEntry() {
    const entryToDelete = this.deleteModalIngredient!;
    this.deleteModalIngredient = null;
    this.editStashOriginalEntry = entryToDelete;
    this.editStashEntry = Object.assign({}, entryToDelete);
    this.editStashEntry.amount = 0;
    this.onEditSubmit();
  }

  prepareAmountAndUnit(amount: number, unit: IngredientUnitDto): string {
    return this.ingredientComputationService.roundAmountForDisplayString(amount, unit);
  }

  hasEditPermission(): boolean {
    if (this.tokenService.isAuthenticated() && this.stash !== undefined) {
      let orgPermission = this.tokenService.getPermissionForOrganization(this.stash.correspondingOrganizationId);
      return (
        this.tokenService.isAdmin() ||
        orgPermission === 'ADMIN' ||
        orgPermission === 'PLANNER' ||
        orgPermission === 'OWNER'
      );
    } else {
      return false;
    }
  }

  protected readonly ButtonVariant = ButtonVariant;
  protected readonly InputType = InputType;
}
