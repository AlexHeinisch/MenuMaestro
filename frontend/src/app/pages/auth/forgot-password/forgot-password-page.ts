import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AccountsApiService } from '../../../../generated';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { FormsModule } from '@angular/forms';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { ErrorService } from '../../../globals/error.service';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';

@Component({
    selector: 'app-forgot-password',
    imports: [
        CommonModule,
        PageLayoutComponent,
        FormsModule,
        SimpleButtonComponent,
        InputFieldComponent,
        RouterModule,
        LoadingSpinnerComponent,
    ],
    templateUrl: './forgot-password-page.html'
})
export class ForgotPasswordComponent {
  InputType = InputType;
  username: string = '';

  // After first submission attempt, form validation will start
  submitted = false;
  // Success flag
  success = false;
  // Error flag
  error = false;
  errorMessage = '';

  isLoading = false;

  constructor(
    private accountsApiService: AccountsApiService,
    private router: Router,
    private errorService: ErrorService
  ) {}

  /**
   * Form validation will start after the method is called, additionally a password reset request will be sent
   */
  requestPasswordReset() {
    this.submitted = true;
    this.error = false;
    this.success = false;

    // Check if username is non-empty
    if (this.username && this.username.trim().length > 0) {
      this.initiatePasswordReset(this.username);
    } else {
      console.error('Invalid input');
    }
  }

  /**
   * Send password reset request to the backend. Always shows success message for security reasons.
   *
   * @param username username for password reset
   */
  initiatePasswordReset(username: string) {
    this.isLoading = true;
    this.accountsApiService.resetPasswordInitiate(username).subscribe({
      next: () => {
        this.isLoading = false;
        this.success = true;
        this.error = false;
      },
      error: (error) => {
        this.isLoading = false;
        // Even on error, we show success message for security reasons
        // (don't want to leak information about whether username exists)
        this.success = true;
        this.error = false;
        console.error('Password reset request error:', error);
        this.errorService.printErrorResponse(error);
      },
    });
  }

  ngOnInit() {}
}
