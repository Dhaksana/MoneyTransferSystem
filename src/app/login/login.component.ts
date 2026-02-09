import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {

  loginData = {
    email: '',
    password: '',
    rememberMe: false
  };

  constructor(private router: Router) {}

  onSubmit() {
    console.log('Login Data:', this.loginData);

    this.router.navigate(['/profile']);
  }
}
