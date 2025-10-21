import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { ButtonVariant, SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-verify-email',
  imports: [
    PageLayoutComponent,
    SimpleButtonComponent,
    CommonModule,
    RouterModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './verify-email.html',
})
export class VerifyEmailComponent implements OnInit {
  ButtonVariant = ButtonVariant;
  isLoading = true;
  verificationSuccess = false;
  verificationError = false;
  errorMessage = '';
  accountUsername = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading = false;
      this.verificationError = true;
      this.errorMessage = 'No verification token provided.';
      return;
    }

    this.verifyEmail(token);
  }

  verifyEmail(token: string) {
    const url = `${environment.apiUrl}/api/v1/verify-email?token=${encodeURIComponent(token)}`;

    this.http.get<any>(url).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.verificationSuccess = true;
        this.accountUsername = response.username || '';
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.verificationError = true;

        if (error.status === 404) {
          this.errorMessage = 'Invalid or expired verification token.';
        } else if (error.status === 403) {
          this.errorMessage = 'Verification token has expired.';
        } else if (error.status === 409) {
          this.errorMessage = error.error?.message || 'Account already exists.';
        } else {
          this.errorMessage = 'An error occurred during verification. Please try again.';
        }
      },
    });
  }
}
