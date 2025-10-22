import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AccountsApiService } from '../../../../generated';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { FormsModule } from '@angular/forms';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { ErrorService } from '../../../globals/error.service';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';

@Component({
    selector: 'app-reset-password',
    imports: [
        CommonModule,
        PageLayoutComponent,
        FormsModule,
        SimpleButtonComponent,
        InputFieldComponent,
        RouterModule,
        LoadingSpinnerComponent,
    ],
    templateUrl: './reset-password-page.html'
})
export class ResetPasswordComponent {
  InputType = InputType;
  username: string = '';
  password: string = '';
  confirmPassword: string = '';
  token: string = '';

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
    private route: ActivatedRoute,
    private errorService: ErrorService
  ) {}

  ngOnInit() {
    // Get token from query parameters
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.error = true;
        this.errorMessage = 'Invalid or missing reset token. Please request a new password reset link.';
      }
    });
  }

  /**
   * Form validation will start after the method is called, additionally a password reset commit will be sent
   */
  resetPassword() {
    this.submitted = true;
    this.error = false;
    this.success = false;
    this.errorMessage = '';

    // Check if all fields are valid
    if (!this.username || this.username.trim().length === 0) {
      this.errorMessage = 'Username is required';
      this.error = true;
      return;
    }

    if (!this.password || this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters';
      this.error = true;
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      this.error = true;
      return;
    }

    if (!this.token) {
      this.errorMessage = 'Invalid reset token';
      this.error = true;
      return;
    }

    this.commitPasswordReset(this.username, this.token, this.password);
  }

  /**
   * Send password reset commit request to the backend.
   *
   * @param username username
   * @param token reset token from email
   * @param password new password
   */
  commitPasswordReset(username: string, token: string, password: string) {
    this.isLoading = true;
    this.accountsApiService.resetPasswordCommit(username, token, { password }).subscribe({
      next: () => {
        this.isLoading = false;
        this.success = true;
        this.error = false;
      },
      error: (error) => {
        this.isLoading = false;
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.message || 'Password reset failed. The link may have expired or is invalid.';
          this.errorService.printErrorResponse(error);
        } else {
          this.errorMessage = error.error || 'Password reset failed. The link may have expired or is invalid.';
          this.errorService.printErrorResponse(error);
        }
      },
    });
  }
}
