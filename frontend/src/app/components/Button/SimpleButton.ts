import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

export enum ButtonVariant {
  primary = 'primary',
  secondary = 'secondary',
  borderOnly = 'borderOnly',
  danger = 'danger',
  danger2 = 'danger2',
}

const variantStyles = {
  [ButtonVariant.primary]:
    'bg-primary text-neutral-50 rounded hover:bg-primary-light focus:ring-2 focus:ring-primary-light',
  [ButtonVariant.secondary]:
    'bg-secondary text-neutral-50 rounded hover:bg-secondary-light focus:ring-2 focus:ring-secondary-light',
  [ButtonVariant.danger]:
    'bg-red-700 text-white rounded hover:bg-red-500 active:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-300 focus:ring-offset-2',
  [ButtonVariant.danger2]:
    'bg-white text-red-700 border border-red-700 rounded hover:border-red-500 hover:text-red-500 focus:outline-none focus:ring-2 focus:ring-red-300 focus:ring-offset-2 transition',
  [ButtonVariant.borderOnly]:
    'bg-transparent text-primary border border-primary hover:text-primary-light hover:border-primary-light rounded focus:ring-2 focus:ring-primary-light',
};

@Component({
    imports: [CommonModule, RouterModule],
    selector: 'simple-button',
    template: `
    <button
      [attr.type]="type"
      [ngClass]="buttonClasses"
      [disabled]="disabled"
      (click)="handleClick($event)"
      [attr.aria-label]="ariaLabel"
      [routerLink]="routerLink ? routerLink : null"
    >
      <ng-content></ng-content>
    </button>
  `,
    styles: []
})
export class SimpleButtonComponent {
  @Input() variant: ButtonVariant = ButtonVariant.primary;
  @Input() type: string = 'button';
  @Input() disabled: boolean = false;
  @Input() ariaLabel?: string;
  @Input() routerLink?: string | string[];
  @Input() className?: string;

  get buttonClasses(): string {
    const baseClasses =
      'button disabled:bg-slate-300 disabled:border-slate-300 disabled:text-slate-500 text-sm disabled:cursor-not-allowed focus:outline-none border py-2 lg:px-4 px-3 flex items-center justify-center w-auto inline-flex hover:transition-colors hover:ease-in h-[42px]';
    return `${baseClasses} ${variantStyles[this.variant]} ${this.className || ''}`.trim();
  }

  handleClick(event: MouseEvent): void {
    if (this.disabled) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}
