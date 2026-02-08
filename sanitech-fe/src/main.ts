import 'zone.js';
import { APP_INITIALIZER, enableProdMode, importProvidersFrom } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { environment } from '@env/environment';
import { authInterceptor } from '@core/auth/auth.interceptor';
import { OAuthModule } from 'angular-oauth2-oidc';
import { RECAPTCHA_V3_SITE_KEY } from 'ng-recaptcha';
import { AuthService } from '@core/auth/auth.service';

if (environment.production) {
  enableProdMode();
}

/**
 * Inizializza OAuth prima che i route guard vengano eseguiti.
 * Garantisce che il token sia disponibile per i deep link su full page reload.
 */
function initializeAuth(auth: AuthService): () => Promise<boolean> {
  return () => auth.loadDiscovery();
}

const providers = [
  provideRouter(
    routes,
    withInMemoryScrolling({
      anchorScrolling: 'enabled',
      scrollPositionRestoration: 'enabled'
    })
  ),
  provideHttpClient(withInterceptors([authInterceptor])),
  importProvidersFrom(OAuthModule.forRoot()),
  { provide: RECAPTCHA_V3_SITE_KEY, useValue: environment.recaptchaSiteKey },
  {
    provide: APP_INITIALIZER,
    useFactory: initializeAuth,
    deps: [AuthService],
    multi: true
  }
];

bootstrapApplication(AppComponent, {
  providers
}).catch((err) => console.error(err));
