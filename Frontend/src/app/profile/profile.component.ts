// src/app/profile/profile.component.ts
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BankingApiService } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit, OnDestroy {
  constructor(
    private api: BankingApiService,
    private auth: AuthService,
    @Inject('API_BASE_URL') public baseUrl: string
  ) {}

  userName = 'User';
  acc: string | null = null;

  balance: number | null = null;
  errorMsg: string | null = null;

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    // Listen to current user changes
    this.auth.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((u) => {
        this.userName = u.name || 'User';
        this.acc = u.id ?? null;
        // load balance for logged-in account
        if (this.acc != null) {
          this.loadBalance(this.acc);
        } else {
          this.balance = null;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadBalance(accountId: string) {
    this.balance = null;
    this.api.getBalance(accountId).subscribe({
      next: (num: number | null) => (this.balance = num),
      error: (e: { message: string }) => {
        this.errorMsg = e.message || 'Failed to load balance';
        this.balance = null;
      },
    });
  }
}