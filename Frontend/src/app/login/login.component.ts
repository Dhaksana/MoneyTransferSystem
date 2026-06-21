import { Component, ChangeDetectionStrategy } from '@angular/core';

import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
    selector: 'app-login',
    imports: [FormsModule, RouterLink, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule],
    changeDetection: ChangeDetectionStrategy.Eager,
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
