import { Component, ChangeDetectionStrategy } from "@angular/core";

@Component({
  selector: "app-footer",
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <footer class="m-4 rounded-lg bg-white">
      <div
        class="mx-auto w-full max-w-(--breakpoint-xl) p-4 flex flex-row justify-around items-center"
      >
        <span class="text-sm text-primary-700 sm:text-center">
          © 2025 MenuMaestro
        </span>
        <span class="text-sm text-primary-700 sm:text-center">
          <a
            href="/privacy"
            style="display: flex; align-items: center; flex-direction: column;"
          >
            Privacy
          </a>
        </span>
        <span class="text-sm text-primary-700 sm:text-center"> </span>
      </div>
    </footer>
  `,
})
export class FooterComponent {}
