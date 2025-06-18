import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthApiService, LoginRequestDto } from '../../../../generated';
import { PageLayoutComponent } from '../../../components/Layout/PageLayout';
import { FormsModule } from '@angular/forms';
import { SimpleButtonComponent } from '../../../components/Button/SimpleButton';
import { InputFieldComponent, InputType } from '../../../components/Input/InputField';
import { TokenService } from '../../../security/token.service';
import { ErrorService } from '../../../globals/error.service';
import { LoadingSpinnerComponent } from '../../../components/LoadingSpinner/LoadingSpinner';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [
    CommonModule,
    PageLayoutComponent,
    FormsModule,
    SimpleButtonComponent,
    InputFieldComponent,
    RouterModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './login-page.html',
})
export class LoginComponent {
  InputType = InputType;
  isLoggedIn: boolean = false;
  loginRequestDto: LoginRequestDto = {
    username: '',
    password: '',
  };

  // After first submission attempt, form validation will start
  submitted = false;
  // Error flag
  error = false;
  errorMessage = '';

  isLoading = false;

  constructor(
    private authApiService: AuthApiService,
    private tokenService: TokenService,
    private router: Router,
    private errorService: ErrorService
  ) {}

  /**
   * Form validation will start after the method is called, additionally an AuthRequest will be sent
   */
  loginUser() {
    this.submitted = true;

    // Check if username and password are non-empty and password has a minimum length of 6
    if (this.loginRequestDto.username && this.loginRequestDto.password && this.loginRequestDto.password.length >= 6) {
      this.authenticateUser(this.loginRequestDto);
    } else {
      console.error('Invalid input');
    }
  }

  /**
   * Send authentication data to the authService. If the authentication was successful, the user will be forwarded to the home page
   *
   * @param loginRequestDto authentication data from the user login form
   */
  authenticateUser(loginRequestDto: LoginRequestDto) {
    this.isLoading = true;
    this.authApiService.login(loginRequestDto).subscribe({
      next: (obj) => {
        this.isLoading = false;
        this.tokenService.saveToken(obj.accessToken.token);
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.isLoading = false;
        this.error = true;
        if (typeof error.error === 'object') {
          this.errorMessage = error.error.error;
          this.errorService.printErrorResponse(error);
        } else {
          this.errorMessage = error.error;
          this.errorService.printErrorResponse(error);
        }
      },
    });
  }

  ngOnInit() {}
}
