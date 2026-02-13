import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

type TxStatus = 'ACTIVE' | 'INACTIVE' | 'SUCCESS' | 'FAILED' | 'PENDING' | string;
type Filter = 'all' | 'received' | 'sent' | 'success' | 'failure';

export interface TransferHistoryItem {
  transactionId: number;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  status: TxStatus;
  failureReason: string | null;
  createdOn: string; // ISO
}

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="m-0">Transaction History</h3>
      <a class="btn btn-light btn-sm" [routerLink]="['/profile']">← Back to Profile</a>
    </div>

    <div class="mb-2 text-muted">
      Showing history for account <strong>{{ accountId }}</strong>
    </div>

    <!-- Filters -->
    <div class="btn-group mb-3" role="group" aria-label="History filters">
      <button type="button" class="btn" [ngClass]="btnClass('all')" (click)="setFilter('all')">
        All <span class="badge text-bg-secondary ms-1">{{ counts.all }}</span>
      </button>
      <button type="button" class="btn" [ngClass]="btnClass('received')" (click)="setFilter('received')">
        Received <span class="badge text-bg-secondary ms-1">{{ counts.received }}</span>
      </button>
      <button type="button" class="btn" [ngClass]="btnClass('sent')" (click)="setFilter('sent')">
        Sent <span class="badge text-bg-secondary ms-1">{{ counts.sent }}</span>
      </button>
      <button type="button" class="btn" [ngClass]="btnClass('success')" (click)="setFilter('success')">
        Success <span class="badge text-bg-secondary ms-1">{{ counts.success }}</span>
      </button>
      <button type="button" class="btn" [ngClass]="btnClass('failure')" (click)="setFilter('failure')">
        Failure <span class="badge text-bg-secondary ms-1">{{ counts.failure }}</span>
      </button>
    </div>

    <div *ngIf="loading" class="alert alert-info py-2">Loading…</div>
    <div *ngIf="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

    <div class="card" *ngIf="!loading">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 8rem;">Transaction ID</th>
              <th style="width: 8rem;">From</th>
              <th style="width: 8rem;">To</th>
              <th style="width: 10rem;">Amount</th>
              <th style="width: 10rem;">Status</th>
              <th style="width: 18rem;">Failure Reason</th>
              <th style="width: 16rem;">Created On</th>
            </tr>
          </thead>

          <tbody>
            <tr *ngIf="!filteredTransactions.length">
              <td colspan="7" class="text-center text-muted py-4">No transactions found</td>
            </tr>

            <tr *ngFor="let txn of filteredTransactions">
              <td>{{ txn.transactionId }}</td>
              <td>{{ txn.fromAccountId }}</td>
              <td>{{ txn.toAccountId }}</td>
              <td>{{ txn.amount | currency:'INR':'symbol':'1.0-2' }}</td>
              <td>
                <span class="badge" [ngClass]="statusBadgeClass(txn.status)">
                  {{ txn.status?.toUpperCase() ?? '' }}
                </span>
              </td>
              <td>{{ txn.failureReason }}</td>
              <td>{{ txn.createdOn | date:'medium' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .btn-group .btn { border: 1px solid #dee2e6; }
    .btn-group .btn.active { background-color: #0d6efd; color: #fff; border-color: #0d6efd; }
  `],
})
export class TransactionHistoryComponent implements OnInit {
  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject('API_BASE_URL') private baseUrl: string,
    private auth: AuthService
  ) {}

  accountId!: string;
  transactions: TransferHistoryItem[] = [];
  loading = false;
  errorMsg: string | null = null;

  // filtering state
  selected: Filter = 'all';
  filteredTransactions: TransferHistoryItem[] = [];

  // counts for badges
  counts = {
    all: 0,
    received: 0,
    sent: 0,
    success: 0,
    failure: 0
  };

  ngOnInit(): void {
    const qpId = this.route.snapshot.queryParamMap.get('accountId') ?? '';
    const authId = this.auth.userId ?? '';
    this.accountId = qpId || authId || '';

    this.loading = true;
    this.getHistoryByAccount(this.accountId).subscribe({
      next: (items: TransferHistoryItem[]) => {
        this.transactions = items || [];
        this.recomputeCounts();   // ✅ recompute the badges
        this.applyFilter();       // ✅ apply current filter to table
        this.loading = false;
      },
      error: (e: { message: string }) => {
        this.errorMsg = e.message || 'Failed to load history';
        this.loading = false;
      },
    });
  }

  private getHistoryByAccount(accountId: string) {
    return this.http
      .get<TransferHistoryItem[]>(`${this.baseUrl}/transfers/history/${encodeURIComponent(accountId)}`)
      .pipe(
        map((res: any) => (Array.isArray(res) ? res : res?.items || [])),
        catchError((err: HttpErrorResponse) => {
          this.errorMsg = err.error?.message || err.error?.error || err.message || 'API error';
          return of([]);
        })
      );
  }

  // --- Filtering logic ---
  setFilter(f: Filter) {
    if (this.selected === f) return;
    this.selected = f;
    this.applyFilter();
  }

  private applyFilter() {
    const id = this.accountId;
    const upper = (s?: string) => (s || '').toUpperCase();

    const arr = this.transactions.filter((t) => {
      switch (this.selected) {
        case 'received': return t.toAccountId === id;
        case 'sent':     return t.fromAccountId === id;
        case 'success':  return upper(t.status) === 'SUCCESS';
        case 'failure':  return upper(t.status) === 'FAILED';
        default:         return true; // 'all'
      }
    });

    // newest first
    this.filteredTransactions = arr.sort((a, b) =>
      new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
    );
  }

  private recomputeCounts() {
    const id = this.accountId;
    const upper = (s?: string) => (s || '').toUpperCase();

    this.counts.all = this.transactions.length;
    this.counts.received = this.transactions.filter(t => t.toAccountId === id).length;
    this.counts.sent = this.transactions.filter(t => t.fromAccountId === id).length;
    this.counts.success = this.transactions.filter(t => upper(t.status) === 'SUCCESS').length;
    this.counts.failure = this.transactions.filter(t => upper(t.status) === 'FAILED').length;
  }

  // --- UI helpers ---
  statusBadgeClass(status?: string) {
    const s = (status || '').toUpperCase();
    if (s === 'ACTIVE' || s === 'SUCCESS') return 'text-bg-success';
    if (s === 'PENDING') return 'text-bg-warning';
    return 'text-bg-danger';
  }

  btnClass(f: Filter) {
    return {
      'btn-outline-secondary': this.selected !== f,
      'active': this.selected === f
    };
  }
}