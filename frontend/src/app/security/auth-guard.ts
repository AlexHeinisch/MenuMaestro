import { Injectable, inject } from "@angular/core";
import { Router } from "@angular/router";
import { TokenService } from "./token.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuard {
  private tokenService = inject(TokenService);
  private router = inject(Router);

  canActivate(): boolean {
    if (this.tokenService.isAuthenticated()) {
      return true;
    } else {
      this.router.navigate(["/login"]);
      return false;
    }
  }
}
