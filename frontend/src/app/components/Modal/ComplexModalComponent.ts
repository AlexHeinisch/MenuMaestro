import {
  Component,
  Input,
  ChangeDetectionStrategy,
  input,
  output,
} from "@angular/core";

import { ButtonVariant, SimpleButtonComponent } from "../Button/SimpleButton";

@Component({
  selector: "complex-modal",
  imports: [SimpleButtonComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    @if (show()) {
      <div
        class="fixed inset-0 bg-black/50 backdrop-blur-xs flex justify-center items-center z-10 p-4"
      >
        <div
          class="bg-white flex min-h-fit flex-col w-full max-w-[700px] justify-between rounded-lg p-6 relative"
        >
          <!-- Add relative positioning to the parent container -->
          <!-- SVG moved and positioned in the top-right corner -->
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="size-6 cursor-pointer absolute top-4 right-4"
            (click)="handleCancel()"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M6 18 18 6M6 6l12 12"
            />
          </svg>
          <h2 class="text-xl wrap-break-word">{{ title() }}</h2>
          <div class="mb-5 mt-5">
            <ng-content></ng-content>
          </div>
          <div
            class="flex flex-col-reverse gap-3 sm:flex-row sm:justify-between"
          >
            @if (firstBtnTitle) {
              <simple-button
                (click)="handleCancel()"
                className="w-full sm:w-90"
                [variant]="ButtonVariant.borderOnly"
              >
                {{ firstBtnTitle }}
              </simple-button>
            }
            <div class="flex flex-col-reverse gap-3 sm:flex-row sm:space-x-2">
              <simple-button
                (click)="handleSubmit()"
                className="w-full sm:w-90"
                [variant]="secondBtnVariant()"
              >
                {{ secondBtnTitle() }}
              </simple-button>
              @if (thirdBtnTitle) {
                <simple-button
                  (click)="handleThirdAction()"
                  className="w-full sm:w-90"
                >
                  {{ thirdBtnTitle }}
                </simple-button>
              }
            </div>
          </div>
        </div>
      </div>
    }
  `,
})
export class ComplexModalComponent {
  readonly title = input.required<string>();
  readonly show = input.required<boolean>();
  @Input() firstBtnTitle?: string;
  readonly secondBtnTitle = input.required<string>();
  @Input() thirdBtnTitle?: string; // Input for the third button title
  readonly secondBtnVariant = input<ButtonVariant>(ButtonVariant.secondary);

  readonly setShow = output<boolean>();
  readonly onCancel = output<void>();
  readonly onSubmit = output<void>();
  readonly onThirdAction = output<void>();

  ButtonVariant = ButtonVariant;

  handleCancel() {
    this.onCancel.emit();
    this.setShow.emit(false);
  }

  handleSubmit() {
    this.onSubmit.emit();
    this.setShow.emit(false);
  }

  handleThirdAction() {
    this.onThirdAction.emit();
  }
}
