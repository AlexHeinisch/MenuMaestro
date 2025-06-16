import { Component } from '@angular/core';
import { InfoMessageComponent, InfoMessageType } from '../../../components/Card/InfoMessage';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import {
  MealInMenuDto, MealsApiService,
  MealStatus,
  MenuDetailDto,
  MenusApiService, OrganizationRoleEnum, ShoppingListApiService,
  SnapshotCreateDto,
  SnapshotInMenuDto,
} from '../../../../generated';
import { CdkDrag, CdkDragPlaceholder, CdkDragPreview, CdkDropList } from '@angular/cdk/drag-drop';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { CommonModule } from '@angular/common';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ErrorService } from '../../../globals/error.service';
import { ToastrService } from 'ngx-toastr';
import { StringFormattingService } from '../../../service/string-formatting.service';
import { TokenService } from '../../../security/token.service';

@Component({
  selector: 'app-menu-display-view',
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
    FormsModule,
    CommonModule,
    RouterLink,
  ],
  templateUrl: './menu-display-view.component.html',
})
export class MenuDisplayViewComponent {
  InputType = InputType;
  ButtonVariant = ButtonVariant;
  InfoMessageType = InfoMessageType;
  MealStatus = MealStatus;

  menuId!: number;
  menuDetail: MenuDetailDto = {} as MenuDetailDto;
  combinedMealsAndSnapshots: (MealInMenuDto | SnapshotInMenuDto)[] = [];

  loadingMenu: boolean = true;
  errorNoMenuFound: string = '';
  showFullDescription = false;
  maxFullDescriptionLength: number = 95;
  menuDeleteModalTitle: string = '';
  isMenuDeleteModalOpen: boolean = false;

  constructor(
    private menusApiService: MenusApiService,
    private mealsApiService: MealsApiService,
    private shoppingListsApiService: ShoppingListApiService,
    private route: ActivatedRoute,
    private router: Router,
    private breakpointObserver: BreakpointObserver,
    private errorService: ErrorService,
    private toastrService: ToastrService,
    private stringFormattingService: StringFormattingService,
    private tokenService: TokenService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.menuId = +params['id'];
      this.fetchMenu();
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

  fetchMenu(): void {
    this.menusApiService.getMenuById(this.menuId).subscribe({
      next: (menu) => {
        this.loadingMenu = false;
        this.menuDetail = menu;

        const meals = this.menuDetail.meals || [];
        const snapshots = this.menuDetail.snapshots || [];

        this.combinedMealsAndSnapshots = [...meals, ...snapshots];

        this.combinedMealsAndSnapshots.sort((a, b) => {
          return a.position! - b.position!;
        });
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

  isSnapshot(item: MealInMenuDto | SnapshotInMenuDto): item is SnapshotInMenuDto {
    return (item as SnapshotInMenuDto).numberOfMealsIncluded !== undefined;
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

  formatStatus(status: string | undefined): string {
    if (!status) return 'Unknown Status';

    return status
      .toLowerCase()
      .replace(/_/g, ' ')
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
