import { Component, Input, Output, EventEmitter, forwardRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MarkdownViewerComponent } from '../MarkdownViewer/markdown-viewer.component';
import { EmojiService, EmojiCategory } from '../../../service/emoji.service';

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
        <!-- Formatting Toolbar -->
        <div class="flex flex-wrap gap-1 p-2 bg-gray-100 border border-gray-300 rounded-t items-center">
          <button
            type="button"
            (click)="applyFormat('bold')"
            class="toolbar-btn"
            title="Bold"
          >
            <strong>B</strong>
          </button>
          <button
            type="button"
            (click)="applyFormat('italic')"
            class="toolbar-btn"
            title="Italic"
          >
            <em>I</em>
          </button>
          <div class="border-l border-gray-400 h-6 mx-1"></div>
          <button
            type="button"
            (click)="applyFormat('h1')"
            class="toolbar-btn"
            title="Heading 1"
          >
            H1
          </button>
          <button
            type="button"
            (click)="applyFormat('h2')"
            class="toolbar-btn"
            title="Heading 2"
          >
            H2
          </button>
          <button
            type="button"
            (click)="applyFormat('h3')"
            class="toolbar-btn"
            title="Heading 3"
          >
            H3
          </button>
          <div class="border-l border-gray-400 h-6 mx-1"></div>
          <button
            type="button"
            (click)="applyFormat('ul')"
            class="toolbar-btn"
            title="Bullet List"
          >
            •
          </button>
          <button
            type="button"
            (click)="applyFormat('ol')"
            class="toolbar-btn"
            title="Numbered List"
          >
            1.
          </button>
          <div class="border-l border-gray-400 h-6 mx-1"></div>
          <button
            type="button"
            (click)="applyFormat('code')"
            class="toolbar-btn font-mono"
            title="Inline Code"
          >
            &lt;&gt;
          </button>
          <button
            type="button"
            (click)="applyFormat('quote')"
            class="toolbar-btn"
            title="Blockquote"
          >
            "
          </button>
          <button
            type="button"
            (click)="applyFormat('hr')"
            class="toolbar-btn"
            title="Horizontal Rule"
          >
            ―
          </button>
          <div class="border-l border-gray-400 h-6 mx-1"></div>
          <!-- Emoji Picker -->
          <div class="relative">
            <button
              type="button"
              class="toolbar-btn"
              (click)="showEmojiPicker = !showEmojiPicker; showTooltip = false"
              title="Insert Emoji"
            >
              😀
            </button>
            <div
              *ngIf="showEmojiPicker"
              class="absolute left-0 mt-2 w-96 bg-white border border-gray-300 rounded shadow-lg p-3 text-sm z-20"
              style="max-height: 400px; overflow-y: auto;"
            >
              <div class="mb-3">
                <input
                  type="text"
                  [(ngModel)]="emojiSearchTerm"
                  (ngModelChange)="onEmojiSearchChange()"
                  placeholder="Search emojis by name or keyword..."
                  class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                />
              </div>
              <div *ngFor="let category of filteredEmojiCategories" class="mb-4">
                <div class="font-semibold text-gray-700 mb-2">{{ category.name }}</div>
                <div class="grid grid-cols-10 gap-1">
                  <button
                    *ngFor="let emojiData of category.emojis"
                    type="button"
                    (click)="insertEmoji(emojiData.emoji)"
                    class="emoji-btn"
                    [title]="emojiData.name"
                  >
                    {{ emojiData.emoji }}
                  </button>
                </div>
              </div>
              <div *ngIf="filteredEmojiCategories.length === 0" class="text-center text-gray-500 py-4">
                No emojis found
              </div>
            </div>
          </div>
          <div class="border-l border-gray-400 h-6 mx-1"></div>
          <!-- Info tooltip -->
          <div class="relative ml-auto">
            <button
              type="button"
              class="toolbar-btn"
              (click)="showTooltip = !showTooltip; showEmojiPicker = false"
              title="Formatting Help"
            >
              ?
            </button>
            <div
              *ngIf="showTooltip"
              class="absolute right-0 mt-2 w-80 bg-white border border-gray-300 rounded shadow-lg p-3 text-xs z-10"
            >
              <div class="font-semibold mb-2">Markdown Formatting Guide</div>
              <div class="space-y-1 text-gray-700">
                <div><code>**bold**</code> → <strong>bold</strong></div>
                <div><code>*italic*</code> → <em>italic</em></div>
                <div><code># Heading 1</code>, <code>## Heading 2</code>, <code>### Heading 3</code></div>
                <div><code>- item</code> → Bullet list</div>
                <div><code>1. item</code> → Numbered list</div>
                <div><code>\`code\`</code> → <code>inline code</code></div>
                <div><code>&gt; quote</code> → Blockquote</div>
                <div><code>---</code> → Horizontal rule</div>
                <div>😀 Use the emoji button to add emojis!</div>
              </div>
              <div class="mt-2 pt-2 border-t border-gray-200 text-gray-500">
                Links and images are not allowed for security reasons.
              </div>
            </div>
          </div>
        </div>

        <textarea
          #textarea
          [(ngModel)]="value"
          (ngModelChange)="onValueChange($event)"
          [placeholder]="placeholder"
          [rows]="rows"
          [maxlength]="maxLength"
          class="w-full p-3 border border-gray-300 border-t-0 rounded-b focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
        ></textarea>
        <div class="mt-2 text-xs text-gray-500 flex justify-end">
          <span *ngIf="maxLength">{{ value?.length || 0 }} / {{ maxLength }}</span>
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

    .toolbar-btn {
      @apply px-3 py-1 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 transition-colors;
    }

    .toolbar-btn:active {
      @apply bg-gray-200;
    }

    .emoji-btn {
      @apply p-2 text-xl hover:bg-gray-100 rounded transition-colors;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      border: none;
      background: transparent;
    }

    .emoji-btn:hover {
      @apply bg-gray-100 scale-110;
      transform: scale(1.1);
    }

    .emoji-btn:active {
      @apply bg-gray-200;
    }
  `]
})
export class MarkdownEditorComponent implements ControlValueAccessor {
  @Input() placeholder: string = 'Enter markdown text...';
  @Input() rows: number = 8;
  @Input() maxLength: number = 4096;
  @Output() valueChange = new EventEmitter<string>();

  @ViewChild('textarea') textarea!: ElementRef<HTMLTextAreaElement>;

  value: string = '';
  activeTab: 'edit' | 'preview' = 'edit';
  showTooltip: boolean = false;
  showEmojiPicker: boolean = false;
  emojiSearchTerm: string = '';
  filteredEmojiCategories: EmojiCategory[] = [];

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private emojiService: EmojiService) {
    this.filteredEmojiCategories = this.emojiService.getEmojiCategories();
  }

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

  applyFormat(format: string): void {
    const textareaEl = this.textarea.nativeElement;
    const start = textareaEl.selectionStart;
    const end = textareaEl.selectionEnd;
    const selectedText = this.value.substring(start, end);
    const beforeText = this.value.substring(0, start);
    const afterText = this.value.substring(end);

    let newText = '';
    let cursorOffset = 0;

    switch (format) {
      case 'bold':
        newText = `**${selectedText || 'bold text'}**`;
        cursorOffset = selectedText ? newText.length : 2;
        break;
      case 'italic':
        newText = `*${selectedText || 'italic text'}*`;
        cursorOffset = selectedText ? newText.length : 1;
        break;
      case 'h1':
        newText = `# ${selectedText || 'Heading 1'}`;
        cursorOffset = newText.length;
        break;
      case 'h2':
        newText = `## ${selectedText || 'Heading 2'}`;
        cursorOffset = newText.length;
        break;
      case 'h3':
        newText = `### ${selectedText || 'Heading 3'}`;
        cursorOffset = newText.length;
        break;
      case 'ul':
        newText = `- ${selectedText || 'list item'}`;
        cursorOffset = newText.length;
        break;
      case 'ol':
        newText = `1. ${selectedText || 'list item'}`;
        cursorOffset = newText.length;
        break;
      case 'code':
        newText = `\`${selectedText || 'code'}\``;
        cursorOffset = selectedText ? newText.length : 1;
        break;
      case 'quote':
        newText = `> ${selectedText || 'quote'}`;
        cursorOffset = newText.length;
        break;
      case 'hr':
        newText = '\n---\n';
        cursorOffset = newText.length;
        break;
    }

    this.value = beforeText + newText + afterText;
    this.onValueChange(this.value);

    // Set cursor position after format is applied
    setTimeout(() => {
      const newCursorPos = start + cursorOffset;
      textareaEl.focus();
      textareaEl.setSelectionRange(newCursorPos, newCursorPos);
    }, 0);
  }

  insertEmoji(emoji: string): void {
    const textareaEl = this.textarea.nativeElement;
    const start = textareaEl.selectionStart;
    const end = textareaEl.selectionEnd;
    const beforeText = this.value.substring(0, start);
    const afterText = this.value.substring(end);

    this.value = beforeText + emoji + afterText;
    this.onValueChange(this.value);

    // Close emoji picker and set cursor position after emoji
    this.showEmojiPicker = false;
    this.emojiSearchTerm = '';
    setTimeout(() => {
      const newCursorPos = start + emoji.length;
      textareaEl.focus();
      textareaEl.setSelectionRange(newCursorPos, newCursorPos);
    }, 0);
  }

  onEmojiSearchChange(): void {
    this.filteredEmojiCategories = this.emojiService.searchEmojis(this.emojiSearchTerm);
  }
}
