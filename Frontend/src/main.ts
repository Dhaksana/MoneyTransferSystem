// main.ts
console.log('[MTS] main.ts loaded, starting bootstrap...');
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AuthInterceptor } from './app/services/auth.interceptor';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withXhr(), withInterceptorsFromDi()),
    provideAnimations(),

    { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
}).then(() => {
  console.log('[MTS] Angular bootstrap SUCCESS');
}).catch((err) => {
  console.error('[MTS] Angular bootstrap FAILED:', err);
});
