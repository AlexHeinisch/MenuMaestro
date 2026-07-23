import {
  Component,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
  input,
} from "@angular/core";

export enum InfoMessageType {
  success = "success",
  failure = "failure",
  neutral = "neutral",
}

@Component({
  selector: "info-message",
  template: `
    <div
      [class]="messageClasses"
      (click)="onClose.emit()"
      (keydown)="onKeyDown($event)"
      class="my-2 p-1 text-center border-2 rounded-xl cursor-pointer"
      tabindex="0"
    >
      {{ message() }}
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.Eager,
  styles: [],
})
export class InfoMessageComponent {
  readonly message = input.required<string>();
  readonly type = input.required<InfoMessageType>();
  @Output() onClose = new EventEmitter<void>();

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === "Enter" || event.key === " ") {
      this.onClose.emit();
    }
  }

  get messageClasses(): string {
    switch (this.type()) {
      case InfoMessageType.success:
        return "text-white border-primary-300 bg-primary-100";
      case InfoMessageType.failure:
        return "text-red-800 border-red-300 bg-red-100";
      case InfoMessageType.neutral:
        return "text-red-800 border-neutral-300 bg-neutral-100";
      default:
        return "";
    }
  }
}
