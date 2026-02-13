
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  username = '';
  password = '';
  holderName = '';
  loading = false;
  errorMsg: string | null = null;

  constructor(private auth: AuthService, private router: Router) {}

  submit(f: NgForm) {
    if (f.invalid) return;
    this.loading = true;
    this.errorMsg = null;

    this.auth.register(this.username, this.password, this.holderName).subscribe({
      next: () => {
        // navigate to profile after successful signup
        this.router.navigateByUrl('/profile');
      },
      error: (e: Error) => {
        this.errorMsg = e.message || 'Registration failed';
        this.loading = false;
      }
    });
  }
}
