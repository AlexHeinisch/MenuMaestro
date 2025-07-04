import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TokenService } from '../../../security/token.service';

@Component({
    selector: 'account-button',
    templateUrl: './account-button.component.html',
    imports: [CommonModule, RouterModule]
})
export class AccountButtonComponent {
  constructor(protected tokenService: TokenService) {}

  getUsernameFirstLetter(): string {
    const username = this.tokenService.getUsername();
    return username ? username.charAt(0).toUpperCase() : '';
  }
}
