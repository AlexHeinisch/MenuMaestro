import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { ErrorService } from '../../../globals/error.service';
import { SimpleModalComponent } from '../../../components/Modal/SimpleModalComponent';
import { TokenService } from '../../../security/token.service';
import { ToastrService } from 'ngx-toastr';
import {AccountEditRequestDto, AccountInfoDto, AccountsApiService} from "../../../../generated";

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    CommonModule,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    SimpleModalComponent,
  ],

  templateUrl: './change-password.component.html',
})
export class ChangePasswordComponent {
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
    oldPassword: undefined,
  };
  confirmNewPassword = ''; // this is only used in the frontend to compare the new passwords
  isConfirmActionOpen: boolean = false;
  submitted = false;

  constructor(
    private accountApiService: AccountsApiService,
    private errorService: ErrorService,
    private tokenService: TokenService,
    private toastr: ToastrService,
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
        this.toastr.success('Password changed.');
      },
      error: (error) => {
        this.errorService.printErrorResponse(error);
      },
    });
  }

  onSubmit(form: NgForm) {
    this.submitted = true;
    if (form.invalid || !this.accountEditRequestDto.newPassword || this.accountEditRequestDto.newPassword.length < 6) {
      Object.keys(form.controls).forEach((field) => {
        const control = form.controls[field];
        control.markAsTouched({ onlySelf: true });
      });
      return;
    }
    this.isConfirmActionOpen = true;
  }

  handleConfirmModalSubmit() {
    this.editAccount();
  }

  openConfirmModal() {
    this.accountEditRequestDto.oldPassword = undefined;
    this.isConfirmActionOpen = true;
  }
}
