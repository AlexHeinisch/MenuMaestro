import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

@Component({
  selector: "page-layout",
  template: `
    <div [class]="containerClasses">
      <ng-content></ng-content>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [],
})
export class PageLayoutComponent {
  @Input() styleNames?: string;

  get containerClasses(): string {
    return `mx-auto w-full px-6 lg:px-4 py-8 md:w-[900px] text-neutral-700 ${this.styleNames || ""}`;
  }
}
