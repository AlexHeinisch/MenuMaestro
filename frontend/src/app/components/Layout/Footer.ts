import { Component } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-footer',
  template: `
    <footer class="m-4 rounded-lg bg-white">
      <div class="mx-auto w-full max-w-screen-xl p-4 flex flex-row justify-around items-center">
        <span class="text-sm text-primary-700 sm:text-center"> © 2025 MenuMaestro </span>
        <span class="text-sm text-primary-700 sm:text-center">
          <a href="/privacy" style="display: flex; align-items: center; flex-direction: column;"> Privacy </a>
        </span>
        <span class="text-sm text-primary-700 sm:text-center">
        </span>
      </div>
    </footer>
  `,
})
export class FooterComponent {}
