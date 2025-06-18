import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { TokenService } from '../../../../security/token.service';

@Component({
  standalone: true,
  selector: 'login-logout-button',
  templateUrl: './login-logout-button.component.html',
  imports: [CommonModule, RouterModule],
})
export class LoginLogoutButtonComponent {
  constructor(
    private router: Router,
    private tokenService: TokenService
  ) {}

  onLogin(): void {
    this.router.navigate(['/login']);
  }

  onLogout(): void {
    this.tokenService.logout();
    this.router.navigate(['/']);
  }

  isAuthenticated(): boolean {
    return this.tokenService.isAuthenticated();
  }
}
