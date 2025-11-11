import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'app-markdown-viewer',
  standalone: true,
  imports: [CommonModule, MarkdownModule],
  template: `
    <div class="markdown-content">
      <markdown
              [data]="content">
      </markdown>
    </div>
  `,
  styles: [`
    .markdown-content {
      color: #374151;
      line-height: 1.6;
    }

    .markdown-content ::ng-deep h1 {
      font-size: 2rem;
      font-weight: 700;
      color: #111827;
      margin-top: 1.5rem;
      margin-bottom: 0.75rem;
      line-height: 1.2;
    }

    .markdown-content ::ng-deep h2 {
      font-size: 1.5rem;
      font-weight: 700;
      color: #111827;
      margin-top: 1.25rem;
      margin-bottom: 0.5rem;
      line-height: 1.3;
    }

    .markdown-content ::ng-deep h3 {
      font-size: 1.25rem;
      font-weight: 600;
      color: #111827;
      margin-top: 1rem;
      margin-bottom: 0.5rem;
      line-height: 1.4;
    }

    .markdown-content ::ng-deep h4 {
      font-size: 1.125rem;
      font-weight: 600;
      color: #111827;
      margin-top: 0.75rem;
      margin-bottom: 0.5rem;
    }

    .markdown-content ::ng-deep p {
      margin-bottom: 1rem;
    }

    .markdown-content ::ng-deep ul,
    .markdown-content ::ng-deep ol {
      margin-left: 1.5rem;
      margin-bottom: 1rem;
      padding-left: 0.5rem;
    }

    .markdown-content ::ng-deep ul {
      list-style-type: disc;
    }

    .markdown-content ::ng-deep ol {
      list-style-type: decimal;
    }

    .markdown-content ::ng-deep li {
      margin-bottom: 0.25rem;
    }

    .markdown-content ::ng-deep blockquote {
      border-left: 4px solid #d1d5db;
      padding-left: 1rem;
      font-style: italic;
      color: #6b7280;
      margin: 1rem 0;
    }

    .markdown-content ::ng-deep code {
      background-color: #f3f4f6;
      border-radius: 0.25rem;
      padding: 0.125rem 0.375rem;
      font-size: 0.875rem;
      font-family: 'Courier New', monospace;
    }

    .markdown-content ::ng-deep pre {
      background-color: #f3f4f6;
      border-radius: 0.375rem;
      padding: 0.75rem;
      overflow-x: auto;
      margin: 1rem 0;
    }

    .markdown-content ::ng-deep pre code {
      background-color: transparent;
      padding: 0;
    }

    .markdown-content ::ng-deep strong {
      font-weight: 700;
    }

    .markdown-content ::ng-deep em {
      font-style: italic;
    }

    .markdown-content ::ng-deep hr {
      margin: 1.5rem 0;
      border: none;
      border-top: 1px solid #d1d5db;
    }

    .markdown-content ::ng-deep table {
      width: 100%;
      border-collapse: collapse;
      margin: 1rem 0;
    }

    .markdown-content ::ng-deep th,
    .markdown-content ::ng-deep td {
      border: 1px solid #d1d5db;
      padding: 0.5rem 0.75rem;
    }

    .markdown-content ::ng-deep th {
      background-color: #f3f4f6;
      font-weight: 700;
    }
  `]
})
export class MarkdownViewerComponent {
  @Input() content: string = '';
}
