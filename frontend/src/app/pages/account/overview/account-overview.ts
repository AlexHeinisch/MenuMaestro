import { Component, ChangeDetectionStrategy, inject } from "@angular/core";
import { RouterModule } from "@angular/router";

import { FormsModule } from "@angular/forms";
import { PageLayoutComponent } from "../../../components/Layout/PageLayout";
import {
  ButtonVariant,
  SimpleButtonComponent,
} from "../../../components/Button/SimpleButton";
import {
  InputFieldComponent,
  InputType,
} from "../../../components/Input/InputField";
import { AccountInfoDto, AccountsApiService } from "../../../../generated";
import { ErrorService } from "../../../globals/error.service";
import { AccountDeleteButtonComponent } from "../account-delete-button/account-delete-button.component";
import { LoadingSpinnerComponent } from "../../../components/LoadingSpinner/LoadingSpinner";

@Component({
  selector: "app-account-overview",
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    AccountDeleteButtonComponent,
    LoadingSpinnerComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./account-overview.html",
})
export class AccountOverview {
  private accountApiService = inject(AccountsApiService);
  private errorService = inject(ErrorService);

  ButtonVariant = ButtonVariant;
  InputType = InputType;

  password: string = "";
  confirmPassword: string = "";

  accountInfo: AccountInfoDto = {
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    isGlobalAdmin: false,
  };

  isLoading: boolean = true;

  ngOnInit(): void {
    this.getAccountInfo();
  }

  getAccountInfo(): void {
    this.accountApiService.getAccountInfo().subscribe({
      next: (response) => {
        this.isLoading = false;
        this.accountInfo = response;
      },
      error: (error) => {
        this.isLoading = false;
        this.errorService.printErrorResponse(error);
      },
    });
  }
}
