import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'app-markdown-viewer',
  standalone: true,
  imports: [CommonModule, MarkdownModule],
  template: `
    <div class="markdown-content prose prose-sm max-w-none">
      <markdown [data]="content"></markdown>
    </div>
  `,
  styles: [`
    .markdown-content {
      @apply text-gray-800;
    }

    .markdown-content :deep(h1),
    .markdown-content :deep(h2),
    .markdown-content :deep(h3),
    .markdown-content :deep(h4),
    .markdown-content :deep(h5),
    .markdown-content :deep(h6) {
      @apply font-bold mt-4 mb-2;
    }

    .markdown-content :deep(h1) {
      @apply text-2xl;
    }

    .markdown-content :deep(h2) {
      @apply text-xl;
    }

    .markdown-content :deep(h3) {
      @apply text-lg;
    }

    .markdown-content :deep(p) {
      @apply mb-2;
    }

    .markdown-content :deep(ul),
    .markdown-content :deep(ol) {
      @apply ml-6 mb-2;
    }

    .markdown-content :deep(ul) {
      @apply list-disc;
    }

    .markdown-content :deep(ol) {
      @apply list-decimal;
    }

    .markdown-content :deep(li) {
      @apply mb-1;
    }

    .markdown-content :deep(blockquote) {
      @apply border-l-4 border-gray-300 pl-4 italic;
    }

    .markdown-content :deep(code) {
      @apply bg-gray-100 rounded px-1 py-0.5 text-sm font-mono;
    }

    .markdown-content :deep(pre) {
      @apply bg-gray-100 rounded p-3 overflow-x-auto;
    }

    .markdown-content :deep(pre code) {
      @apply bg-transparent p-0;
    }

    .markdown-content :deep(strong) {
      @apply font-bold;
    }

    .markdown-content :deep(em) {
      @apply italic;
    }

    .markdown-content :deep(hr) {
      @apply my-4 border-gray-300;
    }

    .markdown-content :deep(table) {
      @apply w-full border-collapse my-4;
    }

    .markdown-content :deep(th),
    .markdown-content :deep(td) {
      @apply border border-gray-300 px-3 py-2;
    }

    .markdown-content :deep(th) {
      @apply bg-gray-100 font-bold;
    }
  `]
})
export class MarkdownViewerComponent {
  @Input() content: string = '';
}
