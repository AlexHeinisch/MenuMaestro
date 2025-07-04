import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlContainer, FormsModule, NgForm } from '@angular/forms';
import { StringFormattingService } from '../../service/string-formatting.service';

export enum InputType {
  text = 'text',
  password = 'password',
  number = 'number',
  email = 'email',
  select = 'select',
  date = 'date',
  checkbox = 'checkbox',
  textarea = 'textarea',
}

@Component({
    imports: [CommonModule, FormsModule],
    selector: 'input-field',
    template: `
      <div [class]="marginBottom">
        <label *ngIf="label && type !== InputType.checkbox" [attr.for]="id" class="block mb-2 text-base text-primary">{{
            label
          }}</label>

        <ng-container>
          @switch (type) {
            @case (InputType.text) {
              <input
                  [type]="type"
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [attr.placeholder]="placeholder"
                  [required]="required"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [(ngModel)]="value"
                  (ngModelChange)="onValueChange($event)"
                  autocomplete="off"
              />
            }
            @case (InputType.password) {
              <div class="flex items-center">
                <input
                    [type]="passwordHidden ? type : 'text'"
                    [id]="id"
                    [name]="name"
                    [attr.aria-label]="ariaLabel"
                    [attr.placeholder]="placeholder"
                    [required]="required"
                    [ngClass]="inputClassesWFullDefault"
                    class="rounded-r-none"
                    [disabled]="disabled"
                    [(ngModel)]="value"
                    [attr.minlength]="minlength"
                    (ngModelChange)="onValueChange($event)"
                />
                <span
                    class="border border-gray-300 rounded-l-none bg-gray-200 p-2 text-[1rem] focus:border-primary rounded-md"
                    (click)="passwordHidden = !passwordHidden"
                >
                  <i
                      class="fa"
                      [ngClass]="{
                        'fa-eye-slash': !passwordHidden,
                        'fa-eye': passwordHidden
                      }"
                  ></i>
                </span>
              </div>
            }
            @case (InputType.number) {
              <input
                  [type]="type"
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [attr.placeholder]="placeholder"
                  [required]="required"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [(ngModel)]="value"
                  (ngModelChange)="onValueChange($event)"
                  [attr.min]="1"
              />

            }
            @case (InputType.email) {
              <input
                  [type]="type"
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [attr.placeholder]="placeholder"
                  [required]="required"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [(ngModel)]="value"
                  (ngModelChange)="onEmailChange($event)"
                  [pattern]="emailPattern"
              />

            }
            @case (InputType.select) {
              <select
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [required]="required"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [(ngModel)]="value"
                  (ngModelChange)="onValueChange($event)"
              >
                <option *ngFor="let option of options" [value]="toSelectKey(option)">{{ toSelectLabel(option) }}
                </option>
              </select>

            }
            @case (InputType.date) {
              <input
                  [type]="type"
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [attr.placeholder]="placeholder"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [(ngModel)]="value"
                  (ngModelChange)="onValueChange($event)"
              />

            }
            @case (InputType.textarea) {
              <textarea
                  [id]="id"
                  [name]="name"
                  [attr.aria-label]="ariaLabel"
                  [attr.placeholder]="placeholder"
                  [required]="required"
                  [disabled]="disabled"
                  [ngClass]="inputClassesWFullDefault"
                  [rows]="rows"
                  [(ngModel)]="value"
                  (ngModelChange)="onValueChange($event)"
              ></textarea>

            }
            @case (InputType.checkbox) {
              <div class="flex items-center space-x-3">
                <input
                    [type]="type"
                    [id]="id"
                    [name]="name"
                    [attr.aria-label]="ariaLabel"
                    [attr.placeholder]="placeholder"
                    [disabled]="disabled"
                    [ngClass]="inputClasses"
                    [ngModel]="value"
                    [checked]="value"
                    (change)="onCheckboxChange($event)"
                    class="w-4 h-4"
                />
                @if (label) {
                  <label [attr.for]="id" class="text-sm text-neutral-700"
                         [ngClass]="value ? labelStyling : ''">
                    {{ label }}
                  </label>
                }
              </div>
              @if (required) {
                <div class="mt-1 h-3">
                  @if (form && form.controls[name]?.invalid && (form.controls[name]?.dirty || !form.controls[name]?.untouched)) {
                    <div>
                      @if (form.controls[name]?.errors?.['required']) {
                        <p class="text-xs text-red-600">*Field is mandatory.</p>
                      }
                    </div>
                  }
                  @if (value && isEmailInvalid) {
                    <div>
                      <p class="text-xs text-red-600">Invalid email format.</p>
                    </div>
                  }
                </div>
              }
            }
          }
        </ng-container>
      </div>
    `,
    styles: [],
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class InputFieldComponent {
  @Input() type: InputType = InputType.text;
  @Input() label?: string;
  @Input() placeholder?: string;
  @Input() required: boolean = false;
  @Input() value: any = null;
  @Input() possibleCheckedBy: string | null = null;
  @Output() valueChange = new EventEmitter<any>();
  @Input() disabled: boolean = false;
  @Input() ariaLabel?: string;
  @Input() className?: string;
  @Input() id: string = '';
  @Input() name: string = '';
  @Input() marginBottom: string = '';
  @Input() labelStyling: string = 'text-neutral-700';
  @Input() minlength: string = '';

  @Input() options: ([number, string] | string)[] = []; // relevant only for select
  @Input() rows: number = 4; // relevant only for textarea

  @Input() form: NgForm | null = null;

  InputType = InputType;
  isEmailInvalid: boolean = false;
  emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // email validation pattern
  passwordHidden = true;

  constructor(private stringFormattingService: StringFormattingService) {}

  get inputClasses(): string {
    const heightClass = this.type === InputType.textarea ? '' : ' h-[42px]';
    const baseClasses =
      'border border-neutral-200 p-2 text-[1rem] focus:border-primary focus:outline-none rounded-md' + heightClass;
    const neutralClasses = 'border-neutral-200';

    // Dynamically construct the class string
    const classes = [baseClasses, neutralClasses, this.className || ''];

    // Filter out any empty or falsy values and join into a string
    return classes.filter(Boolean).join(' ').trim();
  }

  get inputClassesWFullDefault(): string {
    const classes = this.inputClasses;
    return classes.includes('w-') ? classes : classes + ' w-full';
  }

  onValueChange(newValue: any) {
    if (this.type === InputType.number) {
      const numericValue = newValue === '' || newValue === null ? null : Number(newValue);
      this.value = numericValue;
      this.valueChange.emit(numericValue);
    } else {
      this.valueChange.emit(newValue);
    }
  }

  onEmailChange(newValue: string) {
    this.isEmailInvalid = !this.emailPattern.test(newValue);
    this.value = newValue;
    this.valueChange.emit(newValue);
  }

  onCheckboxChange(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    if (this.possibleCheckedBy === null) {
      this.valueChange.emit(inputElement.checked);
    } else {
      if (inputElement.checked) {
        this.valueChange.emit(this.possibleCheckedBy);
      } else {
        this.valueChange.emit('');
      }
    }
  }

  toSelectKey(selectOption: [number, string] | string) {
    return Array.isArray(selectOption) ? selectOption[0] : selectOption;
  }

  toSelectLabel(selectOption: [number, string] | string) {
    return Array.isArray(selectOption) ? selectOption[1] : this.stringFormattingService.formatStringInput(selectOption);
  }
}
