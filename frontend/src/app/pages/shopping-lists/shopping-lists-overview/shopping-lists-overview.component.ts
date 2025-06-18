import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { SimpleCardComponent } from '../../../components/Card/Card';
import { CommonModule } from '@angular/common';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { ErrorService } from '../../../globals/error.service';
import { PaginationControlsComponent } from '../../../components/Pagination/PaginationControls';
import { ToastrService } from 'ngx-toastr';
import {
  MenusApiService,
  MenuSummaryDto, MenuSummaryListPaginatedDto,
  ShoppingListApiService,
  ShoppingListDto,
  ShoppingListListPaginatedDto,
  ShoppingListStatus
} from "../../../../generated";

@Component({
  selector: 'app-shopping-lists-overview',
  standalone: true,
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    SearchInputComponent,
    SimpleCardComponent,
    CommonModule,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    LoadingSpinnerComponent,
    PaginationControlsComponent,
  ],
  templateUrl: './shopping-lists-overview.component.html',
})
export class ShoppingListsOverviewComponent {
  ButtonVariant = ButtonVariant;
  InputType = InputType;
  ShoppingListStatus = ShoppingListStatus;

  shoppingLists: ShoppingListDto[] = [];
  shoppingListsPaginated: ShoppingListListPaginatedDto | undefined = undefined;

  isLoading: boolean = true;

  shoppingListSearchTerm: string = '';
  shoppingListSearchSuggestions: string[] = [];

  menuSearchTerm: string = '';
  menuIdOfSearchTerm: number | undefined = undefined;
  menuSearchSuggestions: string[] = [];
  menuSearchResults: MenuSummaryDto[] = [];

  shoppingListStatusOptions: string[] = Object.values(ShoppingListStatus);
  selectedShoppingListStatus: ShoppingListStatus = ShoppingListStatus.Open;

  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;

  constructor(
    private router: Router,
    private shoppingListApiService: ShoppingListApiService,
    private menusApiService: MenusApiService,
    private errorService: ErrorService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.fetchShoppingLists();
    this.fetchMenusForSearchSuggestion();
  }

  fetchShoppingLists(requestedPage: number = 1): void {
    this.currentPage = requestedPage;
    this.isLoading = true;
    this.shoppingListApiService
      .searchShoppingLists(
        this.currentPage - 1,
        this.pageSize,
        this.shoppingListSearchTerm,
        this.selectedShoppingListStatus,
        this.menuIdOfSearchTerm
      )
      .subscribe({
        next: (data) => {
          this.shoppingListsPaginated = data;
          this.shoppingLists = data.content;
          this.isLoading = false;
        },
        error: (err) => {
          this.isLoading = false;
          this.errorService.printErrorResponse(err);
        },
      });
  }

  fetchShoppingListForSearchSuggestion(): void {
    this.shoppingListApiService.searchShoppingLists(0, 5, this.shoppingListSearchTerm, undefined, undefined).subscribe({
      next: (data) => {
        this.shoppingListSearchSuggestions = data.content.map((item) => item.name);
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }

  onShoppingListSearch(searchTerm: string): void {
    this.shoppingListSearchTerm = searchTerm;
    this.fetchShoppingListForSearchSuggestion();
    this.fetchShoppingLists();
  }

  fetchMenusForSearchSuggestion() {
    this.menusApiService.getMenus(0, 5, undefined, this.menuSearchTerm).subscribe({
      next: (response: MenuSummaryListPaginatedDto) => {
        if (response.content) {
          this.menuSearchResults = response.content;
          this.menuSearchSuggestions = this.menuSearchResults.map((menu) => menu.name);
        } else {
          this.menuSearchResults = [];
          this.menuSearchSuggestions = [];
        }
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onMenuSearch(searchTerm: string) {
    this.menuSearchTerm = searchTerm;
    this.fetchMenusForSearchSuggestion();
  }

  onMenuSelected(selected: string) {
    this.menuSearchTerm = selected;
    this.menuIdOfSearchTerm = this.menuSearchResults.find((menu) => menu.name === this.menuSearchTerm)?.id;
    this.fetchShoppingLists();
  }

  onShoppingListStatusChange(): void {
    this.fetchShoppingLists();
  }

  formatStatus(status: string | undefined): string {
    if (!status) return 'Unknown Status';

    return status
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase()); // capitalize the first letter of each word
  }

  onPageChange(newPage: number): void {
    this.fetchShoppingLists(newPage);
    // Scroll to the top
    window.scrollTo({
      top: 0,
      behavior: 'smooth', // Optional: makes the scroll smooth
    });
  }

  createShoppingList(): void {
    this.toastr.success(
      'You got redirected to the menus. Please select a menu and create a shopping list based on the menu meal groups.'
    );
    this.router.navigate(['/menus']);
  }
}
