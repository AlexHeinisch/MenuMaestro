import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'page-layout',
  template: `
    <div [ngClass]="containerClasses">
      <ng-content></ng-content>
    </div>
  `,
  styles: [],
})
export class PageLayoutComponent {
  @Input() styleNames?: string;

  get containerClasses(): string {
    return `mx-auto w-full px-6 lg:px-4 py-8 md:w-[900px] text-neutral-700 ${this.styleNames || ''}`;
  }
}
