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
          <a
            href="https://reset.inso.tuwien.ac.at/repo/2024ws-ase-pr-group/24ws-ase-pr-qse-05"
            style="display: flex; align-items: center; flex-direction: column;"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 380 380" style="width: 33px; height: 33px;">
              <defs>
                <style>
                  .cls-1 {
                    fill: #e24329;
                  }
                  .cls-2 {
                    fill: #fc6d26;
                  }
                  .cls-3 {
                    fill: #fca326;
                  }
                </style>
              </defs>
              <g id="LOGO">
                <path
                  class="cls-1"
                  d="M282.83,170.73l-.27-.69-26.14-68.22a6.81,6.81,0,0,0-2.69-3.24,7,7,0,0,0-8,.43,7,7,0,0,0-2.32,3.52l-17.65,54H154.29l-17.65-54A6.86,6.86,0,0,0,134.32,99a7,7,0,0,0-8-.43,6.87,6.87,0,0,0-2.69,3.24L97.44,170l-.26.69a48.54,48.54,0,0,0,16.1,56.1l.09.07.24.17,39.82,29.82,19.7,14.91,12,9.06a8.07,8.07,0,0,0,9.76,0l12-9.06,19.7-14.91,40.06-30,.1-.08A48.56,48.56,0,0,0,282.83,170.73Z"
                />
                <path
                  class="cls-2"
                  d="M282.83,170.73l-.27-.69a88.3,88.3,0,0,0-35.15,15.8L190,229.25c19.55,14.79,36.57,27.64,36.57,27.64l40.06-30,.1-.08A48.56,48.56,0,0,0,282.83,170.73Z"
                />
                <path
                  class="cls-3"
                  d="M153.43,256.89l19.7,14.91,12,9.06a8.07,8.07,0,0,0,9.76,0l12-9.06,19.7-14.91S209.55,244,190,229.25C170.45,244,153.43,256.89,153.43,256.89Z"
                />
                <path
                  class="cls-2"
                  d="M132.58,185.84A88.19,88.19,0,0,0,97.44,170l-.26.69a48.54,48.54,0,0,0,16.1,56.1l.09.07.24.17,39.82,29.82s17-12.85,36.57-27.64Z"
                />
              </g>
            </svg>
          </a>
        </span>
      </div>
    </footer>
  `,
})
export class FooterComponent {}
