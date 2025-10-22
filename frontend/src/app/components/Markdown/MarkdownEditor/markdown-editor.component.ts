import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MarkdownViewerComponent } from '../MarkdownViewer/markdown-viewer.component';

@Component({
  selector: 'app-markdown-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownViewerComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => MarkdownEditorComponent),
      multi: true
    }
  ],
  template: `
    <div class="markdown-editor-container">
      <div class="flex border-b border-gray-300 mb-2">
        <button
          type="button"
          class="px-4 py-2 font-medium transition-colors"
          [class.text-blue-600]="activeTab === 'edit'"
          [class.border-b-2]="activeTab === 'edit'"
          [class.border-blue-600]="activeTab === 'edit'"
          [class.text-gray-600]="activeTab !== 'edit'"
          (click)="activeTab = 'edit'"
        >
          Edit
        </button>
        <button
          type="button"
          class="px-4 py-2 font-medium transition-colors"
          [class.text-blue-600]="activeTab === 'preview'"
          [class.border-b-2]="activeTab === 'preview'"
          [class.border-blue-600]="activeTab === 'preview'"
          [class.text-gray-600]="activeTab !== 'preview'"
          (click)="activeTab = 'preview'"
        >
          Preview
        </button>
      </div>

      <div *ngIf="activeTab === 'edit'" class="edit-tab">
        <textarea
          [(ngModel)]="value"
          (ngModelChange)="onValueChange($event)"
          [placeholder]="placeholder"
          [rows]="rows"
          [maxlength]="maxLength"
          class="w-full p-3 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
        ></textarea>
        <div class="mt-2 text-xs text-gray-500 flex justify-between">
          <span>Markdown is supported. Links and images are not allowed for security reasons.</span>
          <span *ngIf="maxLength">{{ value?.length || 0 }} / {{ maxLength }}</span>
        </div>
        <div class="mt-2 text-xs text-gray-600 bg-gray-50 p-2 rounded">
          <strong>Supported formatting:</strong>
          <span class="ml-2">**bold**, *italic*, # Heading, - lists, \`code\`, > quote, --- horizontal rule</span>
        </div>
      </div>

      <div *ngIf="activeTab === 'preview'" class="preview-tab min-h-[100px] p-3 border border-gray-300 rounded bg-gray-50">
        <app-markdown-viewer [content]="value || 'Nothing to preview'"></app-markdown-viewer>
      </div>
    </div>
  `,
  styles: [`
    .markdown-editor-container {
      @apply w-full;
    }

    textarea {
      resize: vertical;
      min-height: 100px;
    }

    .edit-tab, .preview-tab {
      animation: fadeIn 0.2s ease-in;
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
      }
      to {
        opacity: 1;
      }
    }
  `]
})
export class MarkdownEditorComponent implements ControlValueAccessor {
  @Input() placeholder: string = 'Enter markdown text...';
  @Input() rows: number = 8;
  @Input() maxLength: number = 4096;
  @Output() valueChange = new EventEmitter<string>();

  value: string = '';
  activeTab: 'edit' | 'preview' = 'edit';

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  onValueChange(value: string): void {
    this.value = value;
    this.onChange(value);
    this.valueChange.emit(value);
  }
}
