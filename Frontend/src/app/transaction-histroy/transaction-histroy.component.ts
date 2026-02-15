import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { BankingApiService, PaginatedResponse } from '../services/banking-api.service';

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
              <th style="width: 18rem;">Remark</th>
              <th style="width: 16rem;">Created On</th>
            </tr>
          </thead>

          <tbody>
            <tr *ngIf="!displayedTransactions.length">
              <td colspan="7" class="text-center text-muted py-4">No transactions found</td>
            </tr>

            <tr *ngFor="let txn of displayedTransactions" [ngClass]="getRowClass(txn)">
              <td>{{ txn.transactionId }}</td>
              <td>{{ txn.fromAccountId }}</td>
              <td>{{ txn.toAccountId }}</td>
              <td><span [ngClass]="getAmountClass(txn)">{{ getAmountDisplay(txn) }}</span></td>
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

      <!-- Pagination Controls -->
      <div class="card-footer bg-light d-flex justify-content-between align-items-center">
        <div class="text-muted small">
          Showing {{ getShowingFrom() }} to {{ getShowingTo() }} of {{ totalElements }} transactions
        </div>
        <div class="d-flex gap-2 align-items-center">
          <button class="btn btn-sm btn-outline-primary" (click)="prevPage()" [disabled]="currentPage === 0">
            ← Previous
          </button>
          <span class="badge bg-secondary">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
          <button class="btn btn-sm btn-outline-primary" (click)="nextPage()" [disabled]="currentPage >= totalPages - 1">
            Next →
          </button>
        </div>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .btn-group .btn { border: 1px solid #dee2e6; }
    .btn-group .btn.active { background-color: #0d6efd; color: #fff; border-color: #0d6efd; }
    .amount-credit { color: #28a745; font-weight: 600; }
    .amount-debit { color: #dc3545; font-weight: 600; }
    .row-failed { background-color: rgba(220, 53, 69, 0.1); }
    .row-failed:hover { background-color: rgba(220, 53, 69, 0.15); }
  `],
})
export class TransactionHistoryComponent implements OnInit {
  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    @Inject('API_BASE_URL') private baseUrl: string,
    private auth: AuthService,
    private api: BankingApiService
  ) {}

  accountId!: string;
  transactions: TransferHistoryItem[] = [];
  loading = false;
  errorMsg: string | null = null;

  // Pagination state
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // filtering state
  selected: Filter = 'all';
  filteredTransactions: TransferHistoryItem[] = [];
  displayedTransactions: TransferHistoryItem[] = [];
  serverPaged = false;

  // counts for badges
  counts = {
    all: 0,
    received: 0,
    sent: 0,
    success: 0,
    failure: 0
  };

  // Make Math available in template
  Math = Math;

  ngOnInit(): void {
    const qpId = this.route.snapshot.queryParamMap.get('accountId') ?? '';
    const authId = this.auth.userId ?? '';
    this.accountId = qpId || authId || '';

    // Load initial counts from all transactions
    this.loadInitialCounts();
    
    // Load first page with current filter
    this.loadPageWithFilter(0);
  }

  // Load counts for all filter types upfront
  private loadInitialCounts(): void {
    this.http
      .get<TransferHistoryItem[]>(`${this.baseUrl}/transfers/history/${encodeURIComponent(this.accountId)}`)
      .pipe(
        map((res: any) => (Array.isArray(res) ? res : res?.items || [])),
        catchError(() => of([]))
      )
      .subscribe((items: TransferHistoryItem[]) => {
        const id = this.accountId;
        const upper = (s?: string) => (s || '').toUpperCase();

        this.counts.all = items.length;
        this.counts.received = items.filter(t => t.toAccountId === id).length;
        this.counts.sent = items.filter(t => t.fromAccountId === id).length;
        this.counts.success = items.filter(t => upper(t.status) === 'SUCCESS').length;
        this.counts.failure = items.filter(t => upper(t.status) === 'FAILED').length;
      });
  }

  loadPageWithFilter(page: number): void {
    if (page < 0 || page >= this.totalPages && this.totalPages > 0) {
      return;
    }

    this.loading = true;
    this.currentPage = page;

    // Call backend with filter parameter for server-side filtering + pagination
    this.api.getHistoryByAccountPaginatedWithFilter(this.accountId, page, this.pageSize, this.selected).subscribe({
      next: (response: PaginatedResponse<TransferHistoryItem>) => {
        this.displayedTransactions = response.content || [];
        this.totalElements = response.totalElements ?? 0;
        this.totalPages = response.totalPages ?? Math.ceil(this.totalElements / this.pageSize);
        this.loading = false;
      },
      error: (e: any) => {
        console.error('Error loading transactions:', e);
        this.errorMsg = 'Failed to load transactions';
        this.loading = false;
      },
    });
  }

  loadPage(page: number): void {
    this.loadPageWithFilter(page);
  }

  // --- Filtering logic ---
  setFilter(f: Filter) {
    if (this.selected === f) return;
    this.selected = f;
    this.currentPage = 0; // Reset to first page when filter changes
    this.loadPageWithFilter(0); // Load with new filter
  }

  private applyFilter() {
    // This method is no longer used for client-side filtering
    // Kept for backward compatibility if needed
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadPageWithFilter(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.loadPageWithFilter(this.currentPage - 1);
    }
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

  getShowingFrom(): number {
    if (!this.totalElements || this.totalElements === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  getShowingTo(): number {
    if (!this.totalElements || this.totalElements === 0) return 0;
    return this.currentPage * this.pageSize + (this.displayedTransactions?.length || 0);
  }

  // Determine if transaction is credit (received) or debit (sent)
  isCredit(txn: TransferHistoryItem): boolean {
    return txn.toAccountId === this.accountId;
  }

  // Format amount with +/- sign
  getAmountDisplay(txn: TransferHistoryItem): string {
    const sign = this.isCredit(txn) ? '+ ' : '- ';
    const formatted = new Intl.NumberFormat('en-IN', { 
      style: 'currency', 
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(txn.amount);
    return sign + formatted;
  }

  // Get CSS class for amount styling
  getAmountClass(txn: TransferHistoryItem): string {
    return this.isCredit(txn) ? 'amount-credit' : 'amount-debit';
  }

  // Get CSS class for row styling based on transaction status
  getRowClass(txn: TransferHistoryItem): string {
    const status = (txn.status || '').toUpperCase();
    return status === 'FAILED' ? 'row-failed' : '';
  }
}