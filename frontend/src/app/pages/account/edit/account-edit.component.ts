import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { TokenService } from '../../../security/token.service';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { ToastrService } from 'ngx-toastr';
import { ErrorService } from '../../../globals/error.service';
import {AccountEditRequestDto, AccountInfoDto, AccountsApiService} from "../../../../generated";

@Component({
    selector: 'app-account-edit',
    imports: [
        PageLayoutComponent,
        SimpleButtonComponent,
        CommonModule,
        RouterModule,
        InputFieldComponent,
        FormsModule,
        SimpleModalComponent,
    ],
    templateUrl: './account-edit.component.html'
})
export class AccountEditComponent {
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  accountInfo: AccountInfoDto = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    isGlobalAdmin: false,
  };

  accountEditRequestDto: AccountEditRequestDto = {
    email: undefined,
    firstName: undefined,
    lastName: undefined,
    newPassword: undefined,
    oldPassword: undefined, // this one actually means the old password
  };
  confirmNewPassword = undefined; // this is only used in the frontend to compare the new passwords

  isConfirmActionOpen: boolean = false;
  submitted = false;

  constructor(
    private accountApiService: AccountsApiService,
    private tokenService: TokenService,
    private toastr: ToastrService,
    private errorService: ErrorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.getAccountInfo();
  }

  getAccountInfo(): void {
    this.accountApiService.getAccountInfo().subscribe({
      next: (response) => {
        this.accountInfo = response;
        this.accountEditRequestDto.email = response.email;
        this.accountEditRequestDto.firstName = response.firstName;
        this.accountEditRequestDto.lastName = response.lastName;
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  handleConfirmModalSubmit() {
    this.editAccount();
  }

  editAccount(): void {
    if (
      this.accountEditRequestDto.newPassword &&
      this.accountEditRequestDto.oldPassword &&
      this.accountEditRequestDto.newPassword !== this.confirmNewPassword
    ) {
      this.toastr.error('New password and confirm password do not match.');
      return;
    }

    this.accountApiService.editAccount(this.tokenService.getUsername()!, this.accountEditRequestDto).subscribe({
      next: () => {
        this.router.navigate(['/account']);
        this.toastr.success('Account edited.');
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onSubmit(form: NgForm) {
    this.submitted = true;
    if (form.invalid) {
      Object.keys(form.controls).forEach((field) => {
        const control = form.controls[field];
        control.markAsTouched({ onlySelf: true });
      });
      return;
    }

    this.accountEditRequestDto.oldPassword = undefined;
    if (form.valid) {
      this.isConfirmActionOpen = true;
    }
  }
}
