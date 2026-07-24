import {
  Component,
  ViewChild,
  ChangeDetectionStrategy,
  inject,
} from "@angular/core";
import { Router, RouterModule } from "@angular/router";

import { FormsModule, NgForm } from "@angular/forms";
import { PageLayoutComponent } from "../../../components/Layout/PageLayout";
import {
  ButtonVariant,
  SimpleButtonComponent,
} from "../../../components/Button/SimpleButton";
import {
  InputFieldComponent,
  InputType,
} from "../../../components/Input/InputField";
import {
  AccountCreateRequestDto,
  AccountsApiService,
} from "../../../../generated";
import { ErrorService } from "../../../globals/error.service";
import { ToastrService } from "ngx-toastr";
import { LoadingSpinnerComponent } from "../../../components/LoadingSpinner/LoadingSpinner";

@Component({
  selector: "app-account-registration",
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    LoadingSpinnerComponent,
  ],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: "./registration.html",
})
export class AccountRegistration {
  router = inject(Router);
  private accountApiService = inject(AccountsApiService);
  private errorService = inject(ErrorService);
  private toastr = inject(ToastrService);

  ButtonVariant = ButtonVariant;
  InputType = InputType;

  confirmPassword: string = "";

  accountCreate: AccountCreateRequestDto = {
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
  };

  isLoading = false;
  registrationSuccess = false;

  createAccount() {
    this.isLoading = true;
    this.accountApiService.createAccount(this.accountCreate).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.registrationSuccess = true;
        this.toastr.success(
          "Please check your email to verify your account.",
          "Registration Successful!",
        );
      },
      error: (error) => {
        this.isLoading = false;
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onSubmit(form: NgForm) {
    if (
      form.invalid ||
      !this.accountCreate.password ||
      this.accountCreate.password.length < 6
    ) {
      Object.keys(form.controls).forEach((field) => {
        const control = form.controls[field];
        control.markAsTouched({ onlySelf: true });
      });
      return;
    }

    // Passwords do not match check
    if (this.accountCreate.password !== this.confirmPassword) {
      console.error("Passwords do not match.");
      return;
    }

    // Form is valid; proceed with account creation
    this.createAccount();
  }
}
