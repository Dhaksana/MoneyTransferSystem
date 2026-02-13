// main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './app/services/auth.interceptor';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),

    // ✅ Set your backend base (include /api/v1 so FE paths stay simple)
    { provide: 'API_BASE_URL', useValue: 'http://localhost:8080/api/v1' }
    ,{ provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
}).catch(console.error);