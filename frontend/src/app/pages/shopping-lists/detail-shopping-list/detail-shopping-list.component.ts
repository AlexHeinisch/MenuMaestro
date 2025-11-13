import {Component, OnInit} from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { SearchInputComponent } from '../../../components/Input/SearchInput';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { FormsModule } from '@angular/forms';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { TokenService } from '../../../security/token.service';
import { IngredientComputationService } from '../../../service/ingredient-computation.service';
import { ShoppingListAddItemComponent } from './shopping-list-add-item/shopping-list-add-item.component';
import { ToastrService } from 'ngx-toastr';
import { QRCodeComponent } from 'angularx-qrcode';
import { ErrorService } from '../../../globals/error.service';
import { StringFormattingService } from '../../../service/string-formatting.service';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { ShoppingListUpdateMessage, ShoppingListUpdateType } from '../../../websocket/shopping-list-update.message';
import { Globals } from '../../../globals/globals';
import {
  IngredientCategory, IngredientUnitDto, OrganizationRoleEnum,
  ShoppingListApiService,
  ShoppingListDto, ShoppingListEditDto,
  ShoppingListIngredientDto, ShoppingListIngredientEditDto, ShoppingListStatus, ShoppingListTokenDto
} from "../../../../generated";

type IngredientMap = {
  [key in IngredientCategory]?: ShoppingListIngredientDto[];
};

@Component({
    selector: 'app-detail-shopping-list',
    imports: [
        RouterModule,
        CommonModule,
        PageLayoutComponent,
        SimpleButtonComponent,
        FormsModule,
        LoadingSpinnerComponent,
        InputFieldComponent,
        SimpleModalComponent,
        ShoppingListAddItemComponent,
        QRCodeComponent,
    ],
    templateUrl: './detail-shopping-list.component.html'
})
export class DetailShoppingListComponent implements OnInit {
  anonymousUserName: string = 'anonymousUser';

  ShoppingListStatus = ShoppingListStatus;
  InputType = InputType;
  ButtonVariant = ButtonVariant;

  shoppingListDto: ShoppingListDto = {} as ShoppingListDto;
  loadingShoppingList: boolean = true;

  groupedIngredientsOfShoppingList: IngredientMap = {};

  isLegendModalOpen: boolean = false;
  itemYourCartLegendChecked: boolean = true;
  itemNotYourCartLegendChecked: boolean = true;
  shoppingListId: number = -1;
  shareToken: string | undefined = undefined;

  isAddItemView: boolean = false;

  loadingShareToken: boolean = true;
  isCreateShareTokenModalOpen: boolean = false;
  token: undefined | ShoppingListTokenDto = undefined;
  errorMessage: string | undefined = undefined;

  isCloseShoppingListModalOpen: boolean = false;
  closeShoppingModalText: string = '';

  stompClient: Client | undefined = undefined;

  constructor(
    private route: ActivatedRoute,
    private shoppingListApiService: ShoppingListApiService,
    private tokenService: TokenService,
    private ingredientComputationService: IngredientComputationService,
    private stringFormattingService: StringFormattingService,
    private toastr: ToastrService,
    private errorService: ErrorService,
    private router: Router,
    private globals: Globals
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.shareToken = params['token'];
      if (this.shareToken && this.tokenService.isAuthenticated()) {
        this.toastr.info("You don't need an access token since you are already logged in!");
        this.shareToken = undefined;
      }
      if (!this.shareToken && !this.tokenService.isAuthenticated()) {
        this.router.navigate(['/login']);
      }

      this.route.params.subscribe((params) => {
        this.shoppingListId = +params['id'];
        this.fetchShoppingList(this.shoppingListId);
      });

      this.stompClient = new Client({
        brokerURL: this.globals.backendUri + 'ws',
        connectHeaders: { 'X-Authorization': (this.shareToken ?? this.tokenService.getToken()).toString() },
        debug: function (str) {},
        reconnectDelay: 5000,
        webSocketFactory: () => {
          return new SockJS(this.globals.backendUri + 'ws');
        },
        onConnect: () => {
          if (this.stompClient == undefined) {
            return; // can never happen
          }
          this.stompClient.subscribe(
            '/shopping-lists/' + this.shoppingListId,
            (msg) => {
              this.onShoppingListUpdateReceived(JSON.parse(msg.body));
            },
            { 'X-Authorization': (this.shareToken ?? this.tokenService.getToken()).toString() }
          );
        },
        onStompError: (frame) => {
          this.handleWebSocketError('WebSocket connection error: Unable to connect to shopping list updates');
        },
        onWebSocketError: (event) => {
          this.handleWebSocketError('WebSocket connection error: Unable to connect to shopping list updates');
        },
      });
      this.stompClient.activate();
    });
  }

  ngOnDestroy() {
    this.stompClient?.deactivate();
  }

  handleWebSocketError(message: string): void {
    this.toastr.error(message);
    if (this.shareToken) {
      this.toastr.error('Your share token is invalid or has expired. Redirecting to home page...');
      setTimeout(() => {
        this.router.navigate(['/']);
      }, 3000);
    } else {
      this.toastr.error('Unable to establish live updates connection. Please refresh the page.');
    }
  }

  onShoppingListUpdateReceived(msg: ShoppingListUpdateMessage) {
    switch (msg.updateType) {
      case ShoppingListUpdateType.RELOAD:
        this.fetchShoppingList(this.shoppingListId);
        break;
      case ShoppingListUpdateType.MODIFY:
        const itemUpdate = this.shoppingListDto.ingredients.find((item) => item.id == msg!.shoppingListItemId);
        itemUpdate!.checkedBy = msg.checkedBy!;
        itemUpdate!.isChecked = msg.isChecked!;
        itemUpdate!.ingredient.amount = msg.amount!;
        itemUpdate!.ingredient.unit = msg.unit!;
        this.getGroupedListOfIngredients();
        break;
      case ShoppingListUpdateType.CLOSED:
        this.toastr.info('Shopping list has been closed!');
        this.router.navigate(['']);
        break;
    }
  }

  fetchShoppingList(shoppingListId: number): void {
    this.shoppingListApiService.getShoppingListById(shoppingListId, this.shareToken).subscribe({
      next: (shoppingListDto: ShoppingListDto) => {
        this.loadingShoppingList = false;
        this.shoppingListDto = shoppingListDto;
        this.getGroupedListOfIngredients();
      },
      error: (err) => {
        this.loadingShoppingList = false;
        this.errorService.printErrorResponse(err);
        this.errorMessage = err.error.message;
      },
    });
  }

  getCategories(map: IngredientMap): IngredientCategory[] {
    return Object.keys(map).sort((a, b) => {
      if (a === IngredientCategory.Other) return 1;
      if (b === IngredientCategory.Other) return -1;
      return a.localeCompare(b);
    }) as IngredientCategory[];
  }

  getGroupedListOfIngredients(): void {
    this.groupedIngredientsOfShoppingList = this.shoppingListDto.ingredients.reduce<IngredientMap>(
      (acc, ingredient) => {
        const category = ingredient.category;

        if (!acc[category]) {
          acc[category] = [];
        }

        (acc[category] as ShoppingListIngredientDto[]).push(ingredient);

        return acc;
      },
      {} as IngredientMap
    );

    Object.values(this.groupedIngredientsOfShoppingList).forEach((ingredients) => {
      (ingredients as ShoppingListIngredientDto[]).sort((a, b) => a.ingredient.name.localeCompare(b.ingredient.name));
    });
  }

  prepareAmountForDisplay(amount: number, unit: IngredientUnitDto): string {
    return this.ingredientComputationService.roundAmountForDisplayString(amount, unit);
  }

  formatStringInput(stringInput: string | undefined): string {
    return this.stringFormattingService.formatStringInput(stringInput, this.errorMessage);
  }

  openLegendModal(): void {
    this.isLegendModalOpen = true;
  }

  openCreateShareTokenModalModal(): void {
    this.isCreateShareTokenModalOpen = true;
    if (this.token) {
      return;
    }
    this.loadingShareToken = true;
    this.shoppingListApiService.createShoppingListToken(this.shoppingListId).subscribe({
      next: (data) => {
        this.token = data;
        this.loadingShareToken = false;
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
        this.loadingShareToken = false;
      },
    });
  }

  getLoggedInUserName(): string | null {
    return this.tokenService.getUsername();
  }

  onValueChangeOfCheckbox(clickedShoppingListIngredient: ShoppingListIngredientDto): void {
    const allChecked = this.shoppingListDto.ingredients.every((ingredient) => ingredient.isChecked);
    if (allChecked && this.shareToken == undefined) {
      this.openCloseShoppingListModal();
    } else {
      this.shoppingListDto.status = ShoppingListStatus.Open;
    }
    const shoppingListIngredientEditDto: ShoppingListIngredientEditDto = {
      id: clickedShoppingListIngredient.id,
      checked: clickedShoppingListIngredient.isChecked,
    };
    const shoppingListEditDto: ShoppingListEditDto = {
      status: this.shoppingListDto.status,
      ingredients: [shoppingListIngredientEditDto],
    };
    this.shoppingListApiService
      .editShoppingList(this.shoppingListDto.id, shoppingListEditDto, this.shareToken)
      .subscribe({
        next: (data) => {
          this.shoppingListDto = data;
          this.getGroupedListOfIngredients();
        },
        error: (err) => {
          this.fetchShoppingList(this.shoppingListId);
          this.errorService.printErrorResponse(err);
        },
      });
  }

  setIsAddItemView(value: boolean): void {
    this.isAddItemView = value;
  }

  handleCloseView(updatedShoppingList: null | ShoppingListDto): void {
    this.setIsAddItemView(false);
    if (updatedShoppingList) {
      this.shoppingListDto = updatedShoppingList;
      this.getGroupedListOfIngredients();
    }
  }

  hasAtLeastShopperPermission() {
    if (this.shareToken) {
      return true;
    }
    if (this.tokenService.isAdmin()) {
      return true;
    }
    const perm = this.tokenService.getPermissionForOrganization(this.shoppingListDto.organizationId);
    return [
      OrganizationRoleEnum.Shopper,
      OrganizationRoleEnum.Admin,
      OrganizationRoleEnum.Owner,
      OrganizationRoleEnum.Planner,
    ]
      .map((v) => v.toString().toUpperCase())
      .includes(perm ?? '');
  }

  getShareTokenWithLink() {
    const url = new URL(location.href); // Create a URL object
    url.search = ''; // Clear all query parameters
    url.searchParams.set('token', this.token?.token || ''); // Set the new token parameter
    return url.toString(); // Return the updated URL as a string
  }
  openCloseShoppingListModal(): void {
    this.closeShoppingModalText =
      "This shopping list will be closed immediately. You can't undo this action. All checked items (ingredients only) will be transferred to your menu stash.";

    this.isCloseShoppingListModalOpen = true;
  }

  closethisShoppingList(): void {
    this.shoppingListApiService.closeShoppingList(this.shoppingListId).subscribe({
      next: (closeShoppingList) => {
        this.shoppingListDto = closeShoppingList.shoppingListDto;
        this.isCloseShoppingListModalOpen = false;
        this.toastr.success(
          'Shopping list closed. You got redirected to your stash where you can make updates if needed.'
        );
        this.router.navigate([`/stashes/${closeShoppingList.stashId}`]);
      },
      error: (err) => {
        this.errorService.printErrorResponse(err);
      },
    });
  }
}
