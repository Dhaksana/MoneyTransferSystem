// src/app/profile/profile.component.ts
import { Component, Inject, OnDestroy, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BankingApiService } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';
import { Subject, takeUntil } from 'rxjs';
import { TransferHistoryItem } from '../models/transfer.model';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
    selector: 'app-profile',
    imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
    templateUrl: './profile.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrls: ['./profile.component.css']
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
  recentTransactions: TransferHistoryItem[] = [];
  rewardPoints = 0;
  loadingHistory = false;

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
    this.loadingHistory = true;
    this.api.getHistoryByAccount(accountId).subscribe({
      next: (items) => {
        this.recentTransactions = items.slice(0, 5);
        this.loadingHistory = false;
      },
      error: () => {
        this.recentTransactions = [];
        this.loadingHistory = false;
      }
    });
    this.api.getRewardSummary().subscribe({
      next: summary => this.rewardPoints = summary.currentPoints,
      error: () => this.rewardPoints = 0
    });
  }
}
