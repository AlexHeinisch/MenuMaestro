import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TokenService } from '../../security/token.service';
import { AccountButtonComponent } from '../../pages/account/account-button/account-button.component';
import { LoginLogoutButtonComponent } from '../../pages/auth/login/components/login-logout-button.component';

@Component({
    selector: 'header',
    imports: [CommonModule, RouterModule, LoginLogoutButtonComponent, AccountButtonComponent],
    template: `
    <nav class="bg-primary-800">
      <div class="mx-auto max-w-7xl px-2 md:px-6 lg:px-8">
        <div class="relative flex h-16 items-center justify-between">
          <div class="flex flex-1 items-center md:items-stretch md:justify-start">
            <div class="flex flex-shrink-0 items-center">
              <img class="hover:cursor-pointer h-8 w-auto" src="logo.png" routerLink="/" alt="Your Company" />
            </div>
            <div class="hidden md:ml-6 md:block">
              <div class="flex space-x-4">
                <a
                  routerLink="/recipes"
                  [ngClass]="{ 'bg-primary-700': isActive('/recipes') }"
                  class="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 hover:text-white"
                  >Recipes</a
                >
                <a
                  *ngIf="tokenService.isAuthenticated()"
                  routerLink="/menus"
                  [ngClass]="{ 'bg-primary-700': isActive('/menus') }"
                  class="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 hover:text-white"
                  >Menus</a
                >
                <a
                  *ngIf="tokenService.isAuthenticated()"
                  routerLink="/shopping-lists"
                  [ngClass]="{ 'bg-primary-700': isActive('/shopping-lists') }"
                  class="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 hover:text-white"
                  >Shopping Lists</a
                >
                <a
                  *ngIf="tokenService.isAuthenticated()"
                  routerLink="/organizations"
                  [ngClass]="{ 'bg-primary-700': isActive('/organizations') }"
                  class="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 hover:text-white"
                  >Organizations</a
                >
                <a
                  *ngIf="tokenService.isAdmin()"
                  routerLink="/ingredients"
                  [ngClass]="{ 'bg-primary-700': isActive('/ingredients') }"
                  class="rounded-md px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 hover:text-white"
                  >Ingredients</a
                >
              </div>
            </div>
          </div>
          <div class="flex flex-row items-center">
            <div class="hidden md:flex-row md:flex md:items-center ml-auto text-sm">
              <login-logout-button />
              <account-button />
            </div>
            <div class="inline-flex md:hidden">
              <button
                type="button"
                (click)="toggleMobileMenu()"
                class="relative items-center justify-center rounded-md p-2 text-white hover:bg-primary-700 hover:text-white "
                aria-controls="mobile-menu"
                [attr.aria-expanded]="isMobileMenuOpen"
              >
                <span class="absolute -inset-0.5"></span>
                <span class="sr-only">Open main menu</span>
                <svg
                  *ngIf="!isMobileMenuOpen"
                  class="block h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                  />
                </svg>
                <svg
                  *ngIf="isMobileMenuOpen"
                  class="block h-6 w-6"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Mobile menu -->
      <div *ngIf="isMobileMenuOpen" class="md:hidden" id="mobile-menu">
        <div class="flex flex-col items-center justify-center px-2 pb-3 pt-2 border-t border-gray-200">
          <a
            routerLink="/recipes"
            [ngClass]="{ 'bg-primary-700': isActive('/recipes') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Recipes</a
          >
          <a
            *ngIf="tokenService.isAuthenticated()"
            routerLink="/menus"
            [ngClass]="{ 'bg-primary-700': isActive('/menus') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Menus</a
          >
          <a
            *ngIf="tokenService.isAuthenticated()"
            routerLink="/shopping-lists"
            [ngClass]="{ 'bg-primary-700': isActive('/shopping-lists') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Shopping Lists</a
          >
          <a
            *ngIf="tokenService.isAuthenticated()"
            routerLink="/organizations"
            [ngClass]="{ 'bg-primary-700': isActive('/organizations') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Organizations</a
          >
          <a
            *ngIf="tokenService.isAdmin()"
            routerLink="/ingredients"
            [ngClass]="{ 'bg-primary-700': isActive('/ingredients') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Ingredients</a
          >
          <a
            *ngIf="tokenService.isAuthenticated()"
            routerLink="/account"
            [ngClass]="{ 'bg-primary-700': isActive('/account') }"
            class="block rounded-md px-3 py-2 text-base font-medium text-white hover:bg-primary-700 hover:text-white"
            (click)="closeMobileMenu()"
            >Account</a
          >
          <div class="mt-2 border-t">
            <login-logout-button />
          </div>
        </div>
      </div>
    </nav>
  `
})
export class HeaderComponent implements OnInit {
  isMobileMenuOpen = false;
  isUserMenuOpen = false;
  currentRoute: string = '';

  constructor(
    private router: Router,
    public tokenService: TokenService
  ) {}

  ngOnInit() {
    // Update current route on route change
    this.router.events.subscribe(() => {
      this.currentRoute = this.router.url;
    });
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
  }

  toggleUserMenu() {
    this.isUserMenuOpen = !this.isUserMenuOpen;
  }

  isActive(route: string): boolean {
    return this.router.url.startsWith(route);
  }

  @HostListener('document:click', ['$event'])
  closeUserMenuOnClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const userMenu = document.querySelector('[aria-haspopup="true"]') as HTMLElement;
    const menu = document.querySelector('.origin-top-right') as HTMLElement;

    if (this.isUserMenuOpen && userMenu && menu) {
      if (!userMenu.contains(target) && !menu.contains(target)) {
        this.isUserMenuOpen = false;
      }
    }
  }
}
