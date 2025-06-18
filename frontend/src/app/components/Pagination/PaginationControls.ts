import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonVariant, SimpleButtonComponent } from '../Button/SimpleButton';

@Component({
  selector: 'pagination-controls',
  standalone: true,
  template: `
    <div
      *ngIf="data && !(data.first === true && data.last === true)"
      class="flex justify-center mt-6 items-center pt-6 pb-2 bottom-0 bg-white z-10"
    >
      <simple-button
        [disabled]="data.first"
        [variant]="ButtonVariant.borderOnly"
        ariaLabel="Previous page"
        (click)="onPageChange(currentPage - 1)"
      >
        <svg width="20" height="20" xmlns="http://www.w3.org/2000/svg">
          <path
            fill-rule="evenodd"
            clip-rule="evenodd"
            d="M10.409 15.334 4.575 9.5l5.834-5.834 1.06 1.061L6.698 9.5l4.773 4.773-1.061 1.06Z"
            fill="#000"
          ></path>
        </svg>
      </simple-button>

      <div class="mx-5">
        {{ currentPage }}
      </div>

      <simple-button
        [disabled]="data.last"
        [variant]="ButtonVariant.borderOnly"
        ariaLabel="Next page"
        (click)="onPageChange(currentPage + 1)"
      >
        <svg width="20" height="20" xmlns="http://www.w3.org/2000/svg">
          <path
            fill-rule="evenodd"
            clip-rule="evenodd"
            d="M7.591 3.666 13.425 9.5 7.59 15.334l-1.06-1.061L11.302 9.5 6.53 4.727l1.061-1.06Z"
            fill="#000"
          ></path>
        </svg>
      </simple-button>
    </div>
  `,
  styles: [],
  imports: [CommonModule, SimpleButtonComponent],
})
export class PaginationControlsComponent {
  @Input() data: any;
  @Input() currentPage: number = 1;
  @Output() pageChange = new EventEmitter<number>();

  ButtonVariant = ButtonVariant;

  onPageChange(newPage: number): void {
    if (newPage >= 0 && newPage <= this.data.totalPages) {
      this.currentPage = newPage;
      this.pageChange.emit(this.currentPage);
    }
  }
}
