import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { BankingApiService } from '../services/banking-api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute, private api: BankingApiService) {}

  username = '';
  password = '';
  loading = false;
  errorMsg: string | null = null;

  submit(f: NgForm) {
    if (f.invalid) return;
    this.loading = true; this.errorMsg = null;

    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        // Quick local check: if username indicates admin, redirect immediately
        const storedName = localStorage.getItem('user_name') || this.auth.userName;
        if (storedName && storedName.toUpperCase() === 'ADMIN') {
          this.router.navigateByUrl('/admin');
          return;
        }

        // Otherwise probe admin endpoint; if accessible redirect to admin dashboard
        this.api.getAllAccounts().subscribe({
          next: () => this.router.navigateByUrl('/admin'),
          error: () => {
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/profile';
            this.router.navigateByUrl(returnUrl);
          }
        });
      },
      error: (e: Error) => {
        this.errorMsg = e.message || 'Login failed';
        this.loading = false;
      }
    });
  }
}