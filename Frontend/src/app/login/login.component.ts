import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private auth: AuthService, private router: Router, private route: ActivatedRoute) {}

  username = '';
  password = '';
  loading = false;
  errorMsg: string | null = null;

  submit(f: NgForm) {
    if (f.invalid) return;
    this.loading = true; this.errorMsg = null;

    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/profile';
        this.router.navigateByUrl(returnUrl);
      },
      error: (e: Error) => {
        this.errorMsg = e.message || 'Login failed';
        this.loading = false;
      }
    });
  }
}