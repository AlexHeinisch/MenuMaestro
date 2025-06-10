import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { AccountInfoDto, AccountsApiService } from '../../../../generated/accounts';
import { ErrorService } from '../../../globals/error.service';
import { AccountDeleteButtonComponent } from '../account-delete-button/account-delete-button.component';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';

@Component({
  selector: 'app-account-overview',
  standalone: true,
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    CommonModule,
    RouterModule,
    InputFieldComponent,
    FormsModule,
    AccountDeleteButtonComponent,
    LoadingSpinnerComponent,
  ],

  templateUrl: './account-overview.html',
})
export class AccountOverview {
  ButtonVariant = ButtonVariant;
  InputType = InputType;

  password: string = '';
  confirmPassword: string = '';

  accountInfo: AccountInfoDto = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    isGlobalAdmin: false,
  };

  isLoading: boolean = true;

  constructor(
    private accountApiService: AccountsApiService,
    private errorService: ErrorService
  ) {}

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
