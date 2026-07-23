import { Component, ChangeDetectionStrategy, inject } from "@angular/core";
import { Router, RouterModule } from "@angular/router";
import { TokenService } from "../../../../security/token.service";

@Component({
  selector: "login-logout-button",
  templateUrl: "./login-logout-button.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [RouterModule],
})
export class LoginLogoutButtonComponent {
  private router = inject(Router);
  private tokenService = inject(TokenService);

  onLogin(): void {
    this.router.navigate(["/login"]);
  }

  onLogout(): void {
    this.tokenService.logout();
    this.router.navigate(["/"]);
  }

  isAuthenticated(): boolean {
    return this.tokenService.isAuthenticated();
  }
}
