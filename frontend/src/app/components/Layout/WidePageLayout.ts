import { Component, Input, ChangeDetectionStrategy } from "@angular/core";

@Component({
  selector: "wide-page-layout",
  template: `
    <div [class]="containerClasses">
      <ng-content></ng-content>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [],
})
export class WidePageLayoutComponent {
  @Input() styleNames?: string;

  get containerClasses(): string {
    return `mx-auto px-4 py-8 sm:w-[75%] md:w-[80%] lg:w-[70%] xl:w-[65%] text-neutral-700 ${this.styleNames || ""}`;
  }
}
