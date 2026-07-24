import {
  ApplicationConfig,
  provideZoneChangeDetection,
  SecurityContext,
  isDevMode,
} from "@angular/core";
import { provideRouter } from "@angular/router";

import { routes } from "./app.routes";
import {
  provideHttpClient,
  withInterceptors,
  withXhr,
} from "@angular/common/http";
import { authInterceptor } from "./security/auth-interceptor";
import { provideAnimations } from "@angular/platform-browser/animations";
import { provideToastr } from "ngx-toastr";
import { BASE_PATH } from "../generated";
import { provideMarkdown, MARKED_OPTIONS, SANITIZE } from "ngx-markdown";
import { provideServiceWorker } from "@angular/service-worker";

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptors([authInterceptor])),
    provideAnimations(),
    provideToastr({
      preventDuplicates: true,
      progressBar: true,
      progressAnimation: "increasing", // or 'decreasing'
      timeOut: 5000, // Toast duration in milliseconds
      closeButton: true,
      tapToDismiss: true,
    }),
    provideMarkdown({
      sanitize: { provide: SANITIZE, useValue: SecurityContext.HTML }, // Sanitize HTML for defense in depth (backend also validates)
    }),
    {
      provide: MARKED_OPTIONS,
      useValue: {
        breaks: true, // Convert \n in paragraphs into <br>
        gfm: true, // GitHub Flavored Markdown
      },
    },
    {
      provide: BASE_PATH,
      useFactory: () =>
        window.location.protocol + "//" + window.location.host + "/api/v1",
    },
    provideServiceWorker("ngsw-worker.js", {
      enabled: !isDevMode(),
      registrationStrategy: "registerWhenStable:30000",
    }),
  ],
};
