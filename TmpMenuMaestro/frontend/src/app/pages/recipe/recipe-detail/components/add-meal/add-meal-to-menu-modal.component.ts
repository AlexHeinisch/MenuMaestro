import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import {
  OrganizationRoleEnum,
  OrganizationsApiService,
  OrganizationSummaryListPaginatedDto,
} from '../../../../../../generated/organizations';
import {
  MenusApiService,
  MenuSummaryListPaginatedDto,
  OrganizationSummaryDto,
} from '../../../../../../generated/menus';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SimpleModalComponent } from '../../../../../components/Modal/SimpleModalComponent';
import { InputFieldComponent, InputType } from '../../../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { ButtonVariant, SimpleButtonComponent } from '../../../../../components/Button/SimpleButton';
import { ToastrService } from 'ngx-toastr';
import { SearchInputComponent } from '../../../../../components/Input/SearchInput';
import { IngredientDto, IngredientListPaginatedDto } from '../../../../../../generated/ingredients';
import { ErrorService } from '../../../../../globals/error.service';
import { TokenService } from '../../../../../security/token.service';

@Component({
  standalone: true,
  selector: 'app-add-meal-to-menu-modal',
  templateUrl: './add-meal-to-menu-modal.component.html',
  imports: [
    RouterModule,
    CommonModule,
    FormsModule,
    SimpleModalComponent,
    InputFieldComponent,
    SimpleButtonComponent,
    SearchInputComponent,
  ],
})
export class AddMealToMenuModalComponent implements OnInit {
  @ViewChild('searchInput') searchInput!: SearchInputComponent;
  @Input() recipeId: number | undefined | null;

  ButtonVariant = ButtonVariant;
  InputType = InputType;

  modalTitle: string = 'Add this recipe to an organizations menu';
  isModalOpen: boolean = false;

  menuNames: string[] = [];
  menuMap: Record<string, number> = {};
  selectedMenuName: string | null = null;
  selectedMenuId: number | null = null;

  organizationOptions: OrganizationSummaryDto[] = [];
  organizationOptionsNames: string[] = [];
  selectedOrganizationName: string | null = null;
  selectedOrganizationId: number | null = null;

  constructor(
    private menusApiService: MenusApiService,
    private organizationsApiService: OrganizationsApiService,
    private toastr: ToastrService,
    private errorService: ErrorService,
    private router: Router,
    protected tokenService: TokenService
  ) {}

  ngOnInit(): void {
    if (this.tokenService.isAuthenticated()) {
      // Load previously selected organization and menu from localStorage
      const storedOrganizationName = localStorage.getItem('addMealToMenuSelectedOrganizationName');
      const storedOrganizationId = localStorage.getItem('addMealToMenuSelectedOrganizationId');
      const storedMenuName = localStorage.getItem('addMealToMenuSelectedMenuName');
      const storedMenuMap = localStorage.getItem('addMealToMenuMenuMap');

      if (storedOrganizationName && storedOrganizationId) {
        this.selectedOrganizationName = storedOrganizationName;

        // Fetch menus for the stored organization ID
        this.fetchMenus(parseInt(storedOrganizationId, 10));

        // Restore menu map from localStorage if available
        if (storedMenuMap) {
          this.menuMap = JSON.parse(storedMenuMap);

          // Restore the selected menu ID from the map
          if (storedMenuName) {
            this.selectedMenuName = storedMenuName;
            this.selectedMenuId = this.menuMap[storedMenuName];
          }
        }
      }
      this.searchOrganization('');
    }
  }

  openModal(): void {
    this.isModalOpen = true;
  }

  searchOrganization(searchTerm: string) {
    this.organizationsApiService.getOrganizations(0, 5, searchTerm).subscribe({
      next: (response: OrganizationSummaryListPaginatedDto) => {
        if (response.content) {
          this.organizationOptions = response.content;
          this.organizationOptionsNames = this.organizationOptions.map((org) => org.name!);
        } else {
          this.organizationOptions = [];
          this.organizationOptionsNames = [];
        }
      },
      error: (error) => {
        this.toastr.error('Error fetching organizations.');
      },
    });
  }

  onOrganizationSelected(selected: string): void {
    const selectedOrganization = this.organizationOptions.find((org) => org.name === selected);

    if (selectedOrganization) {
      this.selectedOrganizationName = selectedOrganization.name;
      localStorage.setItem('addMealToMenuSelectedOrganizationName', selectedOrganization.name);
      this.selectedOrganizationId = selectedOrganization.id;
      localStorage.setItem('addMealToMenuSelectedOrganizationId', selectedOrganization.id.toString());
      this.fetchMenus(selectedOrganization.id);
    }

    if (selected === '' || selected === undefined) {
      this.organizationOptions = [];
      this.selectedOrganizationName = '';
      localStorage.removeItem('addMealToMenuSelectedOrganizationName');
      localStorage.removeItem('addMealToMenuSelectedOrganizationId');
    }
  }

  fetchMenus(orgId: number): void {
    this.menusApiService.getMenus(undefined, undefined, undefined, '', orgId).subscribe({
      next: (menus: MenuSummaryListPaginatedDto) => {
        this.menuNames = menus.content.map((menu) => menu.name);
        this.menuMap = menus.content.reduce(
          (map, menu) => {
            map[menu.name] = menu.id;
            return map;
          },
          {} as Record<string, number>
        );
        // Save the menuMap to localStorage
        localStorage.setItem('addMealToMenuMenuMap', JSON.stringify(this.menuMap));
        if (this.menuNames.length === 0) {
          this.menuNames = ['No menus found'];
        } else {
          this.onMenuSelected(this.menuNames[0]);
        }
      },
      error: (err) => {
        this.menuNames = ['Error loading menus'];
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onMenuSelected(selectedName: string): void {
    this.selectedMenuName = selectedName;
    this.selectedMenuId = this.menuMap[selectedName];

    if (this.selectedMenuName) {
      localStorage.setItem('addMealToMenuSelectedMenuName', this.selectedMenuName);
    } else {
      localStorage.removeItem('addMealToMenuSelectedMenuName');
    }
  }

  isFormValid(): boolean {
    return !!this.selectedOrganizationName && !!this.selectedMenuName;
  }

  handleSubmit(): void {
    this.menusApiService.addMealToMenu(this.selectedMenuId!, { recipeId: this.recipeId! }).subscribe({
      next: () => {
        this.toastr.success('Meal added to menu.');
        this.isModalOpen = false;
        this.router.navigate(['/recipes']);
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  handleCancel(): void {}

  navigateToMenus(): void {
    this.router.navigate(['/menus'], { state: { openCreateModal: true } });
  }
}
