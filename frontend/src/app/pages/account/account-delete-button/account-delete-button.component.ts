import { Component, ChangeDetectionStrategy, inject } from "@angular/core";
import { Router, RouterModule } from "@angular/router";
import { TokenService } from "../../../security/token.service";
import {
  ButtonVariant,
  SimpleButtonComponent,
} from "../../../components/Button/SimpleButton";
import { SimpleModalComponent } from "../../../components/Modal/SimpleModalComponent";
import {
  InputFieldComponent,
  InputType,
} from "../../../components/Input/InputField";
import { FormsModule } from "@angular/forms";
import { ErrorService } from "../../../globals/error.service";
import { ToastrService } from "ngx-toastr";
import { AccountsApiService } from "../../../../generated";

@Component({
  selector: "account-delete-button",
  templateUrl: "./account-delete-button.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [
    RouterModule,
    SimpleButtonComponent,
    SimpleModalComponent,
    InputFieldComponent,
    FormsModule,
  ],
})
export class AccountDeleteButtonComponent {
  protected tokenService = inject(TokenService);
  private accountService = inject(AccountsApiService);
  private errorService = inject(ErrorService);
  private toastr = inject(ToastrService);
  private router = inject(Router);

  ButtonVariant = ButtonVariant;
  InputType = InputType;

  confirmDelete = "";
  formIsValid = false;
  isModalOpen = false;
  modalTitle = "Delete Account";
  modalBody =
    'Are you sure you want to delete your account? This action can not be reversed and might delete organizations you are owning and their menus! Type "Delete my account" to delete your account.';

  openModal(): void {
    this.isModalOpen = true;
  }

  isFormValid(): boolean {
    return this.confirmDelete.localeCompare("Delete my account") === 0;
  }

  handleModalCancel(): void {
    this.formIsValid = false;
  }

  handleModalSubmit(): void {
    this.accountService
      .deleteAccount(this.tokenService.getUsername()!)
      .subscribe({
        next: (response) => {
          this.toastr.success("Account deleted.");
          this.tokenService.logout();
          this.router.navigate([""]);
        },
        error: (error) => {
          this.errorService.printErrorResponse(error);
        },
      });
  }
}
