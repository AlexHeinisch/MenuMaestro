import { Component } from '@angular/core';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MenusApiService } from '../../../../generated/menus/api/menus.service';
import { InfoMessageComponent, InfoMessageType } from '../../../components/Card/InfoMessage';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { MenuDetailDto } from '../../../../generated/menus/model/menu-detail-dto';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import {
  CdkDrag,
  CdkDragDrop,
  CdkDragPlaceholder,
  CdkDragPreview,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
import { MealInMenuDto } from '../../../../generated/menus/model/meal-in-menu-dto';
import { SnapshotInMenuDto } from '../../../../generated/menus/model/snapshot-in-menu-dto';
import { MealStatus } from '../../../../generated/menus/model/meal-status';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { MealsApiService } from '../../../../generated/meals/api/meals.service';
import { SnapshotCreateDto } from '../../../../generated/menus/model/snapshot-create-dto';
import { ShoppingListApiService } from '../../../../generated/shopping-lists/api/shopping-list.service';
import { ShoppingListCreateDto } from '../../../../generated/shopping-lists/model/shopping-list-create-dto';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ErrorService } from '../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { ComplexModalComponent } from '../../../components/Modal/ComplexModalComponent';
import { ShoppingListPreviewEntryDto } from '../../../../generated/shopping-lists/model/shopping-list-preview-entry-dto';
import { IngredientComputationService } from '../../../service/ingredient-computation.service';
import { MenuStatus } from '../../../../generated/menus';
import { TokenService } from '../../../security/token.service';
import { OrganizationRoleEnum } from '../../../../generated/organizations/model/organization-role-enum';

@Component({
  selector: 'app-menu-detail-view',
  standalone: true,
  imports: [
    SimpleButtonComponent,
    InputFieldComponent,
    PageLayoutComponent,
    InfoMessageComponent,
    LoadingSpinnerComponent,
    CdkDropList,
    CdkDrag,
    CdkDragPreview,
    CdkDragPlaceholder,
    SimpleModalComponent,
    ComplexModalComponent,
    FormsModule,
    CommonModule,
    RouterLink,
  ],
  templateUrl: './menu-detail-view.component.html',
})
export class MenuDetailViewComponent {
  InputType = InputType;
  ButtonVariant = ButtonVariant;
  InfoMessageType = InfoMessageType;
  MealStatus = MealStatus;

  menuId!: number;
  menuDetail: MenuDetailDto = {} as MenuDetailDto;
  newSnapshot: SnapshotCreateDto = {} as SnapshotCreateDto;
  combinedMealsAndSnapshots: (MealInMenuDto | SnapshotInMenuDto)[] = [];
  mealSeparatorName: string = '';
  checkboxList: { id: number; name: string; isChecked: boolean }[] = [];
  selectAll: boolean = false;

  loadingMenu: boolean = true;
  errorNoMenuFound: string = '';
  showFullDescription = false;
  maxFullDescriptionLength: number = 95;
  isLegendModalOpen: boolean = false;
  isShoppingListModalOpen: boolean = false;
  isShoppingListModalStep2Open: boolean = false;
  shoppingListModalTitle: string = '';
  shoppingListName: string = '';
  isMealGroupModalOpen: boolean = false;
  clickedSnapshotPos: number = -1;
  menuDeleteModalTitle: string = '';
  isMenuDeleteModalOpen: boolean = false;

  clickedMeal: MealInMenuDto = {} as MealInMenuDto;
  mealDeleteModalTitle: string = '';
  isMealDeleteModalOpen: boolean = false;

  clickedSnapshot: SnapshotInMenuDto = {} as SnapshotInMenuDto;
  snapshotDeleteModalTitle: string = '';
  isSnapshotDeleteModalOpen: boolean = false;
  handleMultipleShoppingListsModal: boolean = false;

  menuCloseModalTitle: string = '';
  isMenuCloseModalOpen: boolean = false;

  shoppingListIngredientsPreview: ShoppingListPreviewEntryDto[] | null = null;

  isCloseMealModalOpen: boolean = false;
  closeMealModalItem: MealInMenuDto = {} as MealInMenuDto;
  closeMealModalIndex: number = 0;
  closeMealModalDone: boolean = false;

  isAtLeastPlanner: boolean = false;

  constructor(
    private menusApiService: MenusApiService,
    private mealsApiService: MealsApiService,
    private shoppingListsApiService: ShoppingListApiService,
    public ingredientComputationService: IngredientComputationService,
    private route: ActivatedRoute,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private errorService: ErrorService,
    private toastrService: ToastrService,
    protected tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.menuId = +params['id'];
      this.fetchMenu(true);
    });
    this.breakpointObserver.observe([Breakpoints.Handset]).subscribe((result) => {
      if (result.matches) {
        // Mobile view
        this.maxFullDescriptionLength = 25;
      } else {
        // Desktop view
        this.maxFullDescriptionLength = 95;
      }
    });
  }

  fetchMenu(isInitialFetch: boolean = false): void {
    this.menusApiService.getMenuById(this.menuId).subscribe({
      next: (menu) => {
        this.loadingMenu = false;
        this.menuDetail = menu;

        if (this.menuDetail.status === MenuStatus.Closed) {
          this.router.navigate(['/menus/' + this.menuId + '/closed']);
        }

        const meals = this.menuDetail.meals || [];
        const snapshots = this.menuDetail.snapshots || [];

        this.combinedMealsAndSnapshots = [...meals, ...snapshots];

        this.combinedMealsAndSnapshots.sort((a, b) => {
          return a.position! - b.position!;
        });

        if (isInitialFetch && snapshots.length === 0 && meals.length > 0) {
          this.addSnapshot(true); // by default add one initial snapshot if not already present
        }
        if (isInitialFetch) {
          this.isAtLeastPlanner = this.hasAtLeastPlannerPermission();
          setTimeout(() => {
            this.autoScrollToFirstNotDoneMeal();
          }, 100);
        }
      },
      error: (err) => {
        this.errorNoMenuFound = 'No menu with the given id exists.';
        this.loadingMenu = false;
        this.errorService.printErrorResponse(err);
      },
    });
  }

  toggleDescription() {
    this.showFullDescription = !this.showFullDescription;
  }

  drop(event: CdkDragDrop<(MealInMenuDto | SnapshotInMenuDto)[]>) {
    moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    const combinedMealsAndSnapshotsIdsArray = this.combinedMealsAndSnapshots.map((item) => item.id);
    this.menusApiService.changeMenuItemOrder(this.menuId, combinedMealsAndSnapshotsIdsArray).subscribe({
      next: () => {
        this.fetchMenu();
      },
    });
  }
  openCloseMealModal(item: MealInMenuDto, index: number, done: boolean) {
    this.isCloseMealModalOpen = true;
    this.closeMealModalItem = item;
    this.closeMealModalIndex = index;
    this.closeMealModalDone = done;
  }

  callonMark(item: MealInMenuDto, index: number, done: boolean) {
    this.isCloseMealModalOpen = false;
    this.closeMealModalItem = item;
    this.closeMealModalIndex = index;
    this.closeMealModalDone = done;
    this.onMark(false);
  }

  areAllMealsDone(): boolean | undefined {
    return this.menuDetail?.meals?.every((meal: any) => meal.status === MealStatus.Done);
  }

  onMark(removeFromstash: boolean) {
    this.mealsApiService.markCompleted(this.closeMealModalItem.id, this.closeMealModalDone, removeFromstash).subscribe({
      next: () => {
        this.fetchMenu();
        if (this.closeMealModalDone) {
          setTimeout(() => {
            const scrollSuccessful = this.autoScrollOnClickDownToFirstNotDoneMeal(this.closeMealModalIndex);
            if (!scrollSuccessful) {
              setTimeout(() => {
                this.autoScrollOnClickDownToFirstNotDoneMeal(this.closeMealModalIndex);
                if (this.areAllMealsDone()) {
                  this.openMenuCloseModal();
                }
              }, 150);
            }
          }, 150);
          this.toastrService.success(
            'Meal marked as done. \n This meal will be ignored for shopping lists and stash calculations.'
          );
        } else {
          this.toastrService.success(
            'Meal marked as not done. \n This meal will be included in shopping lists and stash calculations.'
          );
        }
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
    this.isCloseMealModalOpen = false;
  }

  autoScrollOnClickDownToFirstNotDoneMeal(index: number): boolean {
    const findNextIncompleteMealArray = this.combinedMealsAndSnapshots.filter(
      (item) => !this.isSnapshot(item) && (item as MealInMenuDto).status !== MealStatus.Done
    ) as MealInMenuDto[];
    if (findNextIncompleteMealArray.length > 0) {
      const firstIncompleteMeal = this.combinedMealsAndSnapshots.indexOf(findNextIncompleteMealArray[0]);
      if (firstIncompleteMeal > index) {
        const id = 'menu-item-' + firstIncompleteMeal;
        const mealElement = document.getElementById(id);
        if (mealElement) {
          const position = mealElement.getBoundingClientRect().top + window.scrollY;
          window.scrollTo({
            top: position,
            behavior: 'smooth',
          });
          return true;
        }
      }
    }
    return false;
  }

  autoScrollToFirstNotDoneMeal(): void {
    const findNextIncompleteMealArray = this.combinedMealsAndSnapshots.filter(
      (item) => !this.isSnapshot(item) && (item as MealInMenuDto).status !== MealStatus.Done
    ) as MealInMenuDto[];
    console.log(findNextIncompleteMealArray);
    if (findNextIncompleteMealArray.length > 0) {
      const firstIncompleteMeal = this.combinedMealsAndSnapshots.indexOf(findNextIncompleteMealArray[0]);
      const id = 'menu-item-' + firstIncompleteMeal;
      const mealElement = document.getElementById(id);
      if (mealElement) {
        const position = mealElement.getBoundingClientRect().top + window.scrollY;
        window.scrollTo({
          top: position,
          behavior: 'smooth',
        });
      }
    }
  }

  onDetails(item: MealInMenuDto) {
    this.router.navigate([`/menus/${this.menuId}/meal/${item.id}`]);
  }

  onDeleteMenu() {
    this.menusApiService.deleteMenuById(this.menuId).subscribe({
      next: () => {
        this.router.navigate([`/menus`]);
        this.toastrService.success('Menu deleted.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onDeleteMeal(item: MealInMenuDto) {
    this.mealsApiService.deleteMealById(item.id).subscribe({
      next: () => {
        this.fetchMenu();
        this.toastrService.success('Meal deleted.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onDeleteSnapshot(item: SnapshotInMenuDto) {
    this.menusApiService.removeSnapshotFromMenu(this.menuId, item.id).subscribe({
      next: () => {
        this.fetchMenu();
        this.toastrService.success('Meals separator deleted.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  isSnapshot(item: MealInMenuDto | SnapshotInMenuDto): item is SnapshotInMenuDto {
    return (item as SnapshotInMenuDto).numberOfMealsIncluded !== undefined;
  }

  createShoppingListFromSnapshot(item: SnapshotInMenuDto, checkedMultipeShoppingLists: boolean) {
    if (!checkedMultipeShoppingLists) {
      this.menusApiService.existsShoppingListForMenu(this.menuId).subscribe((hasOpenList) => {
        if (hasOpenList) {
          this.clickedSnapshot = item;
          this.handleMultipleShoppingListsModal = true;
        } else {
          this.createShoppingListFromSnapshot(item, true);
        }
      });
    } else {
      this.isShoppingListModalOpen = true;
      this.shoppingListModalTitle = 'Create Shopping List';
      this.shoppingListName = this.menuDetail.name + ': ' + item.name;
      // by default have the name of this snapshot group checked but also add option to check all
      const snapshotsOnly = this.combinedMealsAndSnapshots.filter(this.isSnapshot);

      this.checkboxList = snapshotsOnly.map((snapshot) => ({
        id: snapshot.id,
        name: snapshot.name,
        isChecked: snapshot.id === item.id,
      }));

      this.updateSelectAllState();
    }
  }

  updateSelectAllState() {
    this.selectAll = this.checkboxList.every((checkbox) => checkbox.isChecked);
    console.log(this.checkboxList);
  }

  toggleSelectAll(event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    this.checkboxList.forEach((checkbox) => {
      checkbox.isChecked = isChecked;
    });
  }

  addSnapshot(isDefault: boolean) {
    if (this.clickedSnapshotPos === -1) {
      this.mealSeparatorName = 'Initial Selection';
      this.clickedSnapshotPos = this.combinedMealsAndSnapshots.length;
    }
    this.newSnapshot.name = this.mealSeparatorName;
    this.newSnapshot.position = this.clickedSnapshotPos;
    this.menusApiService.addSnapshotToMenu(this.menuId, this.newSnapshot).subscribe({
      next: () => {
        this.fetchMenu();
        this.toastrService.success(
          isDefault
            ? 'A default meals separator was created for planning. It can be deleted manually and custom meals separators can be added.'
            : 'Meals separator created.'
        );
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
    this.mealSeparatorName = '';
    this.clickedSnapshotPos = -1;
  }

  openLegendModal() {
    this.isLegendModalOpen = true;
  }

  handleShoppingListModalStep1Next() {
    this.isShoppingListModalOpen = false;
    this.isShoppingListModalStep2Open = true;
    const createShoppingListDto = this.getCreateShoppingListDto();
    this.shoppingListsApiService.getShoppingListPreview(createShoppingListDto).subscribe({
      next: (response) => {
        this.shoppingListIngredientsPreview = response;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  shoppingListCreateHasAtLeastOneMealGroupChecked(): boolean {
    return this.checkboxList.some((item) => item.isChecked);
  }

  isShoppingListModalSubmitEnabled(): boolean {
    const hasAtLeastOneChecked = this.shoppingListCreateHasAtLeastOneMealGroupChecked();
    return this.shoppingListName.trim().length > 0 && hasAtLeastOneChecked;
  }

  handleShoppingListModalSubmit() {
    const shoppingListCreateDto = this.getCreateShoppingListDto();

    this.shoppingListsApiService.createShoppingList(shoppingListCreateDto).subscribe({
      next: (data) => {
        this.router.navigate(['/shopping-lists', data.id]);
        this.toastrService.success('Shopping list created.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  private getCreateShoppingListDto() {
    const selectedSnapshots = this.checkboxList.filter((checkbox) => checkbox.isChecked);
    const shoppingListCreateDto: ShoppingListCreateDto = {
      name: this.shoppingListName,
      menuId: this.menuId,
      organizationId: this.menuDetail.organization.id,
      snapshotIds: selectedSnapshots.map((snapshot) => snapshot.id),
    };
    return shoppingListCreateDto;
  }

  openMealGroupModal(clickedMealPos: number | undefined) {
    if (clickedMealPos !== undefined) {
      this.clickedSnapshotPos = clickedMealPos + 1;
      this.isMealGroupModalOpen = true;
    }
  }

  handleMealGroupModalCancel() {
    this.mealSeparatorName = '';
    this.clickedSnapshotPos = -1;
  }

  handleMealGroupModalSubmit() {
    this.addSnapshot(false);
  }

  formatStatus(status: string | undefined): string {
    if (!status) return 'Unknown Status';

    return status
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
  }

  openMenuDeleteModal(): void {
    if (this.menuDetail.name) {
      this.menuDeleteModalTitle = 'Are you sure you want to delete "' + this.menuDetail.name + '"?';
    }
    this.isMenuDeleteModalOpen = true;
  }

  handleMenuDeleteModalSubmit(): void {
    this.onDeleteMenu();
  }

  openMealDeleteModal(item: MealInMenuDto): void {
    if (item.name) {
      this.mealDeleteModalTitle = 'Are you sure you want to delete "' + item.name + '"?';
    }
    this.isMealDeleteModalOpen = true;
    this.clickedMeal = item;
  }

  handleMealDeleteModalSubmit(): void {
    this.onDeleteMeal(this.clickedMeal);
  }

  openSnapshotDeleteModal(item: SnapshotInMenuDto): void {
    if (item.name) {
      this.snapshotDeleteModalTitle = 'Are you sure you want to delete "' + item.name + '"?';
    }
    this.isSnapshotDeleteModalOpen = true;
    this.clickedSnapshot = item;
  }

  handleSnapshotDeleteModalSubmit(): void {
    this.onDeleteSnapshot(this.clickedSnapshot);
  }

  openMenuCloseModal(): void {
    if (this.menuDetail.name) {
      this.menuCloseModalTitle = 'Are you sure you want to close "' + this.menuDetail.name + '"?';
    }
    this.isMenuCloseModalOpen = true;
  }

  handleMenuCloseModalSubmit(): void {
    this.menusApiService.closeMenuById(this.menuId).subscribe({
      next: (data) => {
        this.router.navigate(['/menus']);
        this.toastrService.success('Menu closed.');
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  browseRecipes(): void {
    this.router.navigate(['/recipes']);
  }

  formatStringInput(stringInput: string | undefined): string {
    if (!stringInput) return 'Unknown Status';

    return stringInput
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\band\b/g, '&')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
  }

  hasAtLeastPlannerPermission() {
    if (this.tokenService.isAdmin()) {
      return true;
    }
    const perm = this.tokenService.getPermissionForOrganization(this.menuDetail.organization.id);
    return [OrganizationRoleEnum.Admin, OrganizationRoleEnum.Owner, OrganizationRoleEnum.Planner]
      .map((v) => v.toString().toUpperCase())
      .includes(perm ?? '');
  }
}
