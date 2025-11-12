import { ApplicationConfig, provideZoneChangeDetection, SecurityContext } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { AuthInterceptor } from './security/auth-interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import {BASE_PATH} from "../generated";
import { provideMarkdown, MARKED_OPTIONS } from 'ngx-markdown';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    provideAnimations(),
    provideToastr({
      preventDuplicates: true,
      progressBar: true,
      progressAnimation: 'increasing', // or 'decreasing'
      timeOut: 5000, // Toast duration in milliseconds
      closeButton: true,
      tapToDismiss: true,
    }),
    provideMarkdown({
      sanitize: SecurityContext.HTML, // Sanitize HTML for defense in depth (backend also validates)
    }),
    {
      provide: MARKED_OPTIONS,
      useValue: {
        breaks: true, // Convert \n in paragraphs into <br>
        gfm: true, // GitHub Flavored Markdown
      },
    },
    { provide: BASE_PATH, useFactory: () => window.location.protocol + '//' + window.location.host + "/api/v1" }
  ],
};
