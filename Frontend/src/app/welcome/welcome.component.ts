import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
  <div class="container py-5 text-center">
    <h1 class="mb-3">Welcome to Favourite Company</h1>
    <p class="lead mb-4">Manage accounts, transfer money, and view your transaction history.</p>
    <a class="btn btn-primary btn-lg" routerLink="/login">Get Started → Login</a>
  </div>
  `
})
export class WelcomeComponent {}