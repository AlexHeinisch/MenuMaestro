import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export const SHORTCUT_SEARCH_INPUT_RESET = 'Escape';
export const SEARCH_DEBOUNCE_MS = 300;

@Component({
  standalone: true,
  selector: 'search-input',
  imports: [CommonModule, FormsModule],
  template: `
    <label *ngIf="label" [attr.for]="id" class="block mb-2 text-base text-primary">{{ label }}</label>

    <div
      class="flex flex-row px-4 py-2 rounded-lg border overflow-visible bg-white font-[sans-serif] focus-within:border-primary focus:border-0 relative"
    >
      <!-- Search Icon -->
      <div class="flex items-center z-99">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 192.904 192.904"
          width="12px"
          height="12px"
          class="fill-current rotate-90"
        >
          <path
            d="m190.707 180.101-47.078-47.077c11.702-14.072 18.752-32.142 18.752-51.831C162.381 36.423 125.959 0 81.191 0 36.422 0 0 36.423 0 81.193c0 44.767 36.422 81.187 81.191 81.187 19.688 0 37.759-7.049 51.831-18.751l47.079 47.078a7.474 7.474 0 0 0 5.303 2.197 7.498 7.498 0 0 0 5.303-12.803zM15 81.193C15 44.694 44.693 15 81.191 15c36.497 0 66.189 29.694 66.189 66.193 0 36.496-29.692 66.187-66.189 66.187C44.693 147.38 15 117.689 15 81.193z"
          ></path>
        </svg>
      </div>

      <input
        [id]="id"
        [(ngModel)]="searchTerm"
        [placeholder]="placeholder"
        (input)="onSearch($event)"
        (keydown)="onKeyDown($event)"
        (focus)="onFocus()"
        (blur)="onBlur()"
        class="w-full outline-none border-0 text-gray-600 pl-5 text-[1rem] focus:border-0"
        autocomplete="off"
      />

      <!-- Cross Icon -->
      <div class="flex items-center w-6">
        <svg
          *ngIf="shouldShowResetIcon"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="size-5 cursor-pointer"
          (click)="resetSearch()"
        >
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
        </svg>
      </div>

      <!-- Autocomplete Dropdown -->
      <ul
        *ngIf="isFocused && (filteredOptions.length > 0 || (supportsAddCustom && searchTerm !== '' && !hasExactMatch))"
        class="absolute bg-white border border-gray-300 rounded-lg mt-7 z-10 overflow-hidden shadow-xl  left-0 right-0 "
      >
        <li
          *ngFor="let option of filteredOptions"
          (mousedown)="onOptionSelected(option)"
          (keydown.enter)="onOptionSelected(option)"
          (keydown.space)="onOptionSelected(option)"
          tabindex="0"
          class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm w-full"
        >
          {{ getOptionLabel(option) }}
        </li>
        <li
          *ngIf="supportsAddCustom && !hasExactMatch && searchTerm"
          (mousedown)="addCustomOption(searchTerm)"
          (keydown.enter)="addCustomOption(searchTerm)"
          (keydown.space)="addCustomOption(searchTerm)"
          class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm w-full flex items-center"
        >
          <span class="icon-[material-symbols--add] mr-1 -ml-1 text-primary"></span> Add "{{ searchTerm }}"
        </li>
      </ul>
    </div>
  `,
})
export class SearchInputComponent {
  @Input() placeholder: string = 'Search';
  @Input() handleSearch: (searchTerm: string) => void = () => {};
  @Input() options: string[] | [number, string][] = [];
  @Input() label?: string;
  @Input() id: string = '';
  @Input() searchTerm: string = '';
  @Input() supportsAddCustom: boolean = false;

  @Input() filterLocally: boolean = false;
  @Output() selectedOption = new EventEmitter<any>();
  @Output() selectedAddCustom = new EventEmitter<any>();

  filteredOptions: any[] = [];
  isFocused: boolean = false;
  hasExactMatch: boolean = false;
  private clickInsideDropdown: boolean = false;

  private searchSubject = new Subject<string>();
  private debounceTime: number = SEARCH_DEBOUNCE_MS;

  get shouldShowResetIcon(): boolean {
    return this.searchTerm.length > 0;
  }

  constructor() {
    this.searchSubject.pipe(debounceTime(this.debounceTime), distinctUntilChanged()).subscribe((searchTerm) => {
      this.filterOptions(searchTerm);
      if (this.handleSearch) {
        this.handleSearch(searchTerm);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['options'] || changes['searchTerm']) {
      this.filterOptions(this.searchTerm);
    }
  }

  onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value;
    this.searchSubject.next(this.searchTerm);
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === SHORTCUT_SEARCH_INPUT_RESET) {
      this.resetSearch();
    }
  }

  resetSearch(): void {
    this.searchTerm = '';
    this.filteredOptions = [];
    this.searchSubject.next('');
    this.selectedOption.emit('');
  }

  onOptionSelected(option: string | [number, string]): void {
    this.searchTerm = this.getOptionLabel(option);
    this.filteredOptions = [];
    this.selectedOption.emit(this.getOptionKey(option));
  }

  addCustomOption(option: string): void {
    this.filteredOptions = [];
    this.selectedAddCustom.emit(option);
  }

  private filterOptions(searchTerm: string): void {
    if (!this.filterLocally) {
      this.filteredOptions = this.options;
      this.hasExactMatch = this.options.some(
        (option) => this.getOptionLabel(option).toLowerCase() === searchTerm.toLowerCase()
      );
    } else if (searchTerm) {
      this.filteredOptions = this.options.filter((option) =>
        this.getOptionLabel(option).toLowerCase().includes(searchTerm.toLowerCase())
      ) as string[] | [number, string][];
      this.hasExactMatch = this.options.some(
        (option) => this.getOptionLabel(option).toLowerCase() === searchTerm.toLowerCase()
      );
    } else {
      this.filteredOptions = [];
    }
  }

  getOptionKey(option: string | [number, string]): number | string {
    return Array.isArray(option) ? option[0] : option;
  }

  getOptionLabel(option: string | [number, string]): string {
    return Array.isArray(option) ? option[1] : option;
  }

  onFocus(): void {
    this.isFocused = true;
    this.hasExactMatch = true;
  }

  onBlur(): void {
    // Delay closing the dropdown to ensure the click on an option is handled
    setTimeout(() => {
      if (!this.clickInsideDropdown) {
        this.isFocused = false;
      }
      this.clickInsideDropdown = false; // Reset after handling the click
    }, 100);
  }

  onDropdownClick(): void {
    // Prevent the blur event from being triggered when clicking on the dropdown
    this.clickInsideDropdown = true;
  }
}
