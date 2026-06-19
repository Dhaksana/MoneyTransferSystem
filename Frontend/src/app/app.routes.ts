import { Routes } from '@angular/router';
import { WelcomeComponent } from './welcome/welcome.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { ProfileComponent } from './profile/profile.component';
import { adminGuard, authGuard } from './app.guard';
import { TransactionHistoryComponent } from './transaction-histroy/transaction-histroy.component';
import { TransferMoneyComponent } from './transfer-money/transfer-money.component';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';
import { BeneficiariesComponent } from './beneficiaries/beneficiaries.component';
import { RewardsComponent } from './rewards/rewards.component';
import { AnalyticsComponent } from './analytics/analytics.component';
import { StatementsComponent } from './statements/statements.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { AdminComponent } from './admin/admin.component';



export const routes: Routes = [
  { path: '', redirectTo: 'welcome', pathMatch: 'full' },
  { path: 'welcome', component: WelcomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'login', component: LoginComponent },

  // protected
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionHistoryComponent, canActivate: [authGuard] },
  { path: 'transfer', component: TransferMoneyComponent, canActivate: [authGuard] },
  { path: 'beneficiaries', component: BeneficiariesComponent, canActivate: [authGuard] },
  { path: 'rewards', component: RewardsComponent, canActivate: [authGuard] },
  { path: 'analytics', component: AnalyticsComponent, canActivate: [authGuard] },
  { path: 'statements', component: StatementsComponent, canActivate: [authGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [authGuard, adminGuard] },
  { path: 'unauthorized', component: UnauthorizedComponent },

  { path: '**', redirectTo: 'welcome' }
];
