import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
    imports: [CommonModule],
    selector: 'simple-card',
    template: `
    <div [ngClass]="cardClasses">
      <h2 class="mb-2 text-sm font-medium">{{ title }}</h2>
      <ng-content></ng-content>
    </div>
  `
})
export class SimpleCardComponent {
  @Input() title!: string;
  @Input() body!: string;
  @Input() class?: string;

  get cardClasses(): string {
    const baseClasses = 'p-4 border border-gray-300';
    return `${baseClasses} ${this.class || ''}`.trim();
  }
}
