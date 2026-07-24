import { Component, ChangeDetectionStrategy, input } from "@angular/core";

@Component({
  selector: "simple-card",
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <div [class]="cardClasses">
      <h2 class="mb-2 text-sm font-medium">{{ title() }}</h2>
      <ng-content></ng-content>
    </div>
  `,
})
export class SimpleCardComponent {
  readonly title = input.required<string>();
  readonly class = input<string>();

  get cardClasses(): string {
    const baseClasses = "p-4 border border-gray-300";
    return `${baseClasses} ${this.class() || ""}`.trim();
  }
}
