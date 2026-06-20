// src/app/profile/profile.component.ts
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BankingApiService } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
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
  userRole = 'USER';
  acc: string | null = null;

  balance: number | null = null;
  monthlyReceived: number = 0;
  monthlyExpense: number = 0;
  totalRewardPoints: number = 0;
  rewardRuleDescription = 'Eligible transfers earn 1 point per ₹100 withdrawn on successful transfers above ₹100.';
  rewardHistory: any[] = [];
  recentTransactions: any[] = [];
  errorMsg: string | null = null;

  // Balance update form
  showUpdateBalance = false;
  newBalance: number | null = null;
  updateBalanceLoading = false;
  updateBalanceSuccess = false;
  updateBalanceError: string | null = null;

  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    // Listen to current user changes
    this.auth.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe((u) => {
        this.userName = u.name || 'User';
        this.userRole = this.auth.userRole;
        this.acc = u.id ?? null;
        // load balance for logged-in account
        if (this.acc != null) {
          this.loadBalance(this.acc);
          this.loadMonthlyStats(this.acc);
          this.loadRewardSummary(this.acc);
          this.loadRewardHistory(this.acc);
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

  openUpdateBalanceForm() {
    this.showUpdateBalance = true;
    this.newBalance = this.balance;
    this.updateBalanceSuccess = false;
    this.updateBalanceError = null;
  }

  cancelUpdateBalance() {
    this.showUpdateBalance = false;
    this.newBalance = null;
    this.updateBalanceSuccess = false;
    this.updateBalanceError = null;
  }

  submitUpdateBalance() {
    if (this.newBalance === null || this.newBalance === undefined || !this.acc) {
      this.updateBalanceError = 'Please enter a valid balance amount';
      return;
    }

    if (this.newBalance < 0) {
      this.updateBalanceError = 'Balance cannot be negative';
      return;
    }

    this.updateBalanceLoading = true;
    this.updateBalanceError = null;

    this.api.updateBalance(this.acc, this.newBalance).subscribe({
      next: (response: any) => {
        this.updateBalanceLoading = false;
        this.updateBalanceSuccess = true;
        this.balance = response?.balance ?? this.newBalance;
        this.showUpdateBalance = false;
        setTimeout(() => (this.updateBalanceSuccess = false), 3000);
      },
      error: (e: { message: string }) => {
        this.updateBalanceLoading = false;
        this.updateBalanceError = e.message || 'Failed to update balance';
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

  private loadRewardSummary(accountId: string) {
    this.totalRewardPoints = 0;
    this.api.getRewardSummary(accountId).subscribe({
      next: (summary) => {
        this.totalRewardPoints = summary?.totalPoints ?? 0;
      },
      error: (e: { message: string }) => {
        console.error('Failed to load reward summary:', e.message);
        this.totalRewardPoints = 0;
      }
    });
  }

  private loadRewardHistory(accountId: string) {
    this.rewardHistory = [];
    this.api.getRewardHistory(accountId).subscribe({
      next: (history) => {
        this.rewardHistory = history.slice(0, 5);
      },
      error: (e: { message: string }) => {
        console.error('Failed to load reward history:', e.message);
        this.rewardHistory = [];
      }
    });
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