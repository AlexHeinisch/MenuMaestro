import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SimpleButtonComponent, ButtonVariant } from '../Button/SimpleButton';

@Component({
    selector: 'simple-modal',
    imports: [CommonModule, SimpleButtonComponent],
    template: `
    <div *ngIf="show" class="fixed inset-0 bg-black/50 backdrop-blur-sm flex justify-center items-center z-10 p-4">
      <div class="bg-white flex min-h-fit flex-col w-full max-w-[600px] justify-between rounded-lg p-6 relative">
        <h2 class="text-xl break-words">{{ title }}</h2>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="size-6 cursor-pointer absolute top-4 right-4"
          (click)="handleCancel()"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
        </svg>
        <div class="mb-5 mt-5">
          <ng-content></ng-content>
        </div>
        <div class="flex flex-col-reverse sm:flex-row gap-3 sm:justify-end">
          <simple-button
            *ngIf="cancelBtnTitle"
            (click)="handleCancel()"
            [variant]="ButtonVariant.borderOnly"
            className="w-full sm:w-32"
          >
            {{ cancelBtnTitle }}
          </simple-button>
          <simple-button (click)="handleSubmit()" [disabled]="!isSubmitEnabled" className="w-full sm:w-32">
            {{ submitBtnTitle }}
          </simple-button>
        </div>
      </div>
    </div>
  `
})
export class SimpleModalComponent {
  ButtonVariant = ButtonVariant;

  @Input() title!: string;
  @Input() show!: boolean;
  @Input() cancelBtnTitle?: string;
  @Input() submitBtnTitle!: string;
  @Input() isSubmitEnabled: boolean = true;

  @Output() setShow = new EventEmitter<boolean>();
  @Output() onCancel = new EventEmitter<void>();
  @Output() onSubmit = new EventEmitter<void>();

  handleCancel() {
    this.onCancel.emit();
    this.setShow.emit(false);
  }

  handleSubmit() {
    this.onSubmit.emit();
    this.setShow.emit(false);
  }
}
