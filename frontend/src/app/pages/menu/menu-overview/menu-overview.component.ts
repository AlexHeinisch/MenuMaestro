import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import {
  MenusApiService,
  MenuStatus,
  MenuSummaryListPaginatedDto, OrganizationsApiService,
  OrganizationSummaryDto,
  OrganizationSummaryListPaginatedDto,
} from '../../../../generated';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { RouterModule } from '@angular/router';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleCardComponent } from '../../../components/Card/Card';
import { CommonModule } from '@angular/common';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { ErrorService } from '../../../globals/error.service';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';
import { CreateMenuModalContentComponent } from './components/create-menu-modal-content/create-menu-modal-content.component';

@Component({
  selector: 'app-menu-overview',
  standalone: true,
  imports: [
    PageLayoutComponent,
    CreateMenuModalContentComponent,
    SimpleModalComponent,
    SimpleButtonComponent,
    SearchInputComponent,
    SimpleCardComponent,
    CommonModule,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    LoadingSpinnerComponent,
    PaginationControlsComponent,
    CreateMenuModalContentComponent,
  ],

  templateUrl: './menu-overview.component.html',
})
export class MenuOverviewComponent implements OnInit {
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  organizations: OrganizationSummaryDto[] = [];

  menus: MenuSummaryListPaginatedDto | undefined;
  isLoading: boolean = true;

  currentPage = 1;
  pageSize = 10;
  sort = ['asc'];

  // Menu Search
  menuSearchTerm: string = '';
  menuSearchSuggestions: string[] = [];

  // Organization Search
  organizationSearchNames: string[] = [];
  selectedOrganizationId: number | undefined = undefined;

  // Menu Status Drop Down
  menuStatusOptions: string[] = Object.values(MenuStatus);
  selectedMenuStatus: MenuStatus = MenuStatus.Serving;

  constructor(
    private menusApiService: MenusApiService,
    private organizationsApiService: OrganizationsApiService,
    private errorService: ErrorService
  ) {}

  ngOnInit(): void {
    this.fetchMenus();
    this.fetchMenuForSearchSuggestion('');
    this.searchOrganization('');
  }

  fetchMenus(
    requestedPage: number = 1,
    pageSize: number = this.pageSize,
    menuSearchTerm: string = '',
    selectedMenuStatus: MenuStatus = MenuStatus.Serving,
    organizationId: number | undefined = undefined,
    sort: string[] = ['asc']
  ): void {
    this.currentPage = requestedPage;
    this.isLoading = true;
    this.menusApiService
      .getMenus(this.currentPage - 1, pageSize, sort, menuSearchTerm, organizationId, selectedMenuStatus)
      .subscribe({
        next: (menu) => {
          this.menus = menu;
          this.isLoading = false;
        },
        error: (err) => {
          this.isLoading = false;
          this.errorService.printErrorResponse(err);
        },
      });
  }

  fetchMenuForSearchSuggestion(searchTerm: string): void {
    this.menusApiService.getMenus(0, 5, ['asc'], searchTerm, this.selectedOrganizationId, MenuStatus.All).subscribe({
      next: (menu) => {
        this.menuSearchSuggestions = (menu.content || [])
          .map((item) => item?.name)
          .filter((name): name is string => name !== undefined);
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  searchOrganization(searchTerm: string) {
    this.organizationsApiService.getOrganizations(0, 5, searchTerm).subscribe({
      next: (response: OrganizationSummaryListPaginatedDto) => {
        this.organizations = response.content;
        this.organizationSearchNames = this.organizations.map((org) => org.name!);
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onOrganizationSelected(selected: string) {
    const selectedOrganization = this.organizations.find((org) => org.name === selected);
    if (selectedOrganization) {
      this.selectedOrganizationId = selectedOrganization.id;
    } else {
      this.selectedOrganizationId = undefined;
    }
    this.fetchMenus(
      1,
      this.pageSize,
      this.menuSearchTerm,
      this.selectedMenuStatus,
      this.selectedOrganizationId,
      this.sort
    );
  }

  onMenuSearch(searchTerm: string): void {
    this.menuSearchTerm = searchTerm;
    this.fetchMenuForSearchSuggestion(searchTerm);
    this.fetchMenus(
      1,
      this.pageSize,
      this.menuSearchTerm,
      this.selectedMenuStatus,
      this.selectedOrganizationId,
      this.sort
    );
  }

  onPageChange(newPage: number): void {
    this.fetchMenus(
      newPage,
      this.pageSize,
      this.menuSearchTerm,
      this.selectedMenuStatus,
      this.selectedOrganizationId,
      this.sort
    );
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  }

  onMenuStatusChange(): void {
    this.fetchMenus(
      1,
      this.pageSize,
      this.menuSearchTerm,
      this.selectedMenuStatus,
      this.selectedOrganizationId,
      this.sort
    );
  }

  protected readonly MenuStatus = MenuStatus;
}
