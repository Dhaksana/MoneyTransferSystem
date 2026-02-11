import { Routes } from '@angular/router';
import { WelcomeComponent } from './welcome/welcome.component';
import { LoginComponent } from './login/login.component';
import { ProfileComponent } from './profile/profile.component';
import { authGuard } from './app.guard';
import { TransactionHistoryComponent } from './transaction-histroy/transaction-histroy.component';
import { TransferMoneyComponent } from './transfer-money/transfer-money.component';



export const routes: Routes = [
  { path: '', redirectTo: 'welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'login', component: LoginComponent },

  // protected
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionHistoryComponent, canActivate: [authGuard] },
  { path: 'transfer', component: TransferMoneyComponent, canActivate: [authGuard] },

  { path: '**', redirectTo: 'welcome' }
];
