import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export enum InfoMessageType {
  success = 'success',
  failure = 'failure',
  neutral = 'neutral',
}

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'info-message',
  template: `
    <div
      [ngClass]="messageClasses"
      (click)="onClose.emit()"
      (keydown)="onKeyDown($event)"
      class="my-2 p-1 text-center border-2 rounded-xl cursor-pointer"
      tabindex="0"
    >
      {{ message }}
    </div>
  `,
  styles: [],
})
export class InfoMessageComponent {
  @Input() message!: string;
  @Input() type!: InfoMessageType;
  @Output() onClose = new EventEmitter<void>();

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      this.onClose.emit();
    }
  }

  get messageClasses(): string {
    switch (this.type) {
      case InfoMessageType.success:
        return 'text-white border-primary-300 bg-primary-100';
      case InfoMessageType.failure:
        return 'text-red-800 border-red-300 bg-red-100';
      case InfoMessageType.neutral:
        return 'text-red-800 border-neutral-300 bg-neutral-100';
      default:
        return '';
    }
  }
}
