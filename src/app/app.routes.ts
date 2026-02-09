import { Routes } from '@angular/router';
import { WelcomeComponent } from './welcome/welcome.component';
import { LoginComponent } from './login/login.component';
import { ProfileComponent } from './profile/profile.component';

export const routes: Routes = [
  { path: '', component: WelcomeComponent },     // default route
  { path: 'login', component: LoginComponent },
  {path:'profile', component: ProfileComponent},  // /login page
  { path: '**', redirectTo: '' }                 // fallback
];
