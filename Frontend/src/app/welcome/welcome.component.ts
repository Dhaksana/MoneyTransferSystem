import { Component, inject, ChangeDetectionStrategy } from '@angular/core';

import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
    selector: 'app-welcome',
    imports: [FormsModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatProgressSpinnerModule],
    templateUrl: './welcome.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    styleUrls: ['./welcome.component.css']
})
export class WelcomeComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  authMode: 'login' | 'signup' = 'login';
  drawerOpen = false;

  loginUsername = '';
  loginPassword = '';
  loginLoading = false;
  loginError: string | null = null;

  signupUsername = '';
  signupPassword = '';
  signupHolderName = '';
  signupLoading = false;
  signupError: string | null = null;

  features = [
    { icon: 'send', title: 'Instant Transfers', desc: 'Send money to any beneficiary in real time, 24/7.' },
    { icon: 'receipt_long', title: 'Transaction History', desc: 'View and export every transaction with full detail.' },
    { icon: 'workspace_premium', title: 'Rewards Program', desc: 'Earn points on every transfer and redeem exclusive perks.' },
    { icon: 'monitoring', title: 'Analytics', desc: 'Track your spending patterns with beautiful charts.' },
    { icon: 'description', title: 'Statements', desc: 'Generate monthly statements and export as PDF.' },
    { icon: 'admin_panel_settings', title: 'Admin Portal', desc: 'Full administrative control over users and transactions.' },
  ];

  openDrawer(mode: 'login' | 'signup') {
    this.authMode = mode;
    this.drawerOpen = true;
    this.loginError = null;
    this.signupError = null;
  }

  closeDrawer() {
    this.drawerOpen = false;
    this.loginError = null;
    this.signupError = null;
  }

  onLogin(f: NgForm) {
    if (f.invalid) { console.log('[MTS] Welcome: login form invalid'); return; }
    console.log('[MTS] Welcome: onLogin called, username:', this.loginUsername);
    this.loginLoading = true;
    this.loginError = null;

    this.auth.login(this.loginUsername, this.loginPassword).subscribe({
      next: () => {
        console.log('[MTS] Welcome: login succeeded, navigating to /profile');
        this.router.navigateByUrl('/profile');
      },
      error: (e: Error) => {
        console.error('[MTS] Welcome: login failed:', e.message);
        this.loginError = e.message || 'Login failed';
        this.loginLoading = false;
      },
      complete: () => {
        console.log('[MTS] Welcome: login observable completed');
      }
    });
  }

  onSignup(f: NgForm) {
    if (f.invalid) { console.log('[MTS] Welcome: signup form invalid'); return; }
    console.log('[MTS] Welcome: onSignup called, username:', this.signupUsername);
    this.signupLoading = true;
    this.signupError = null;

    this.auth.register(this.signupUsername, this.signupPassword, this.signupHolderName).subscribe({
      next: () => {
        console.log('[MTS] Welcome: signup succeeded, navigating to /profile');
        this.router.navigateByUrl('/profile');
      },
      error: (e: Error) => {
        console.error('[MTS] Welcome: signup failed:', e.message);
        this.signupError = e.message || 'Registration failed';
        this.signupLoading = false;
      },
      complete: () => {
        console.log('[MTS] Welcome: signup observable completed');
      }
    });
  }
}
