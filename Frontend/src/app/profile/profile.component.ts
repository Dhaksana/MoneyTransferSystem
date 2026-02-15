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
  monthlyReceived: number = 0;
  monthlyExpense: number = 0;
  recentTransactions: any[] = [];
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
          this.loadMonthlyStats(this.acc);
        } else {
          this.balance = null;
          this.monthlyReceived = 0;
          this.monthlyExpense = 0;
          this.recentTransactions = [];
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

  loadMonthlyStats(accountId: string) {
    this.api.getHistoryByAccount(accountId).subscribe({
      next: (transactions: any[]) => {
        this.calculateMonthlyTotals(transactions, accountId);
        this.extractRecentTransactions(transactions);
      },
      error: (e: { message: string }) => {
        console.error('Failed to load transaction history:', e.message);
        this.monthlyReceived = 0;
        this.monthlyExpense = 0;
        this.recentTransactions = [];
      },
    });
  }

  private calculateMonthlyTotals(transactions: any[], accountId: string) {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    this.monthlyReceived = 0;
    this.monthlyExpense = 0;

    transactions.forEach((txn) => {
      const txnDate = new Date(txn.createdOn);
      // Only count successful transactions from current month
      if (
        txnDate.getMonth() === currentMonth &&
        txnDate.getFullYear() === currentYear &&
        txn.status?.toUpperCase() === 'SUCCESS'
      ) {
        if (txn.toAccountId === accountId) {
          this.monthlyReceived += txn.amount;
        } else if (txn.fromAccountId === accountId) {
          this.monthlyExpense += txn.amount;
        }
      }
    });
  }

  private extractRecentTransactions(transactions: any[]) {
    // Sort by date (newest first) and take last 5
    this.recentTransactions = transactions
      .sort(
        (a, b) =>
          new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
      )
      .slice(0, 5);
  }

  statusBadgeClass(status?: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'SUCCESS') return 'bg-success';
    if (s === 'PENDING') return 'bg-warning';
    return 'bg-danger';
  }

  getRowClassRecent(txn: any): string {
    const status = (txn.status || '').toUpperCase();
    return status === 'FAILED' ? 'table-danger' : '';
  }
}