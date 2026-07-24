import { Component, ChangeDetectionStrategy, inject } from "@angular/core";
import { RouterModule } from "@angular/router";
import { TokenService } from "../../../security/token.service";

@Component({
  selector: "account-button",
  templateUrl: "./account-button.component.html",
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [RouterModule],
})
export class AccountButtonComponent {
  protected tokenService = inject(TokenService);

  getUsernameFirstLetter(): string {
    const username = this.tokenService.getUsername();
    return username ? username.charAt(0).toUpperCase() : "";
  }
}
