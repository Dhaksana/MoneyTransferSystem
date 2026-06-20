import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BankingApiService, TransferHistoryItem } from '../services/banking-api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="m-0">Admin Dashboard</h3>
    </div>

    <div *ngIf="loadingAccounts" class="alert alert-info py-2">Loading accounts…</div>
    <div *ngIf="accountsError" class="alert alert-danger py-2">{{ accountsError }}</div>

    <div class="card mb-3" *ngIf="!loadingAccounts">
      <div class="card-header d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
        <div>All Accounts</div>
        <div class="input-group input-group-sm w-100 w-md-auto">
          <input type="text" class="form-control" placeholder="Search account by ID"
                 [(ngModel)]="accountSearch" (keyup.enter)="searchAccounts()" />
          <button class="btn btn-outline-secondary" type="button" (click)="searchAccounts()">Search</button>
        </div>
      </div>
      <div *ngIf="accountSearch?.trim()" class="card-body py-2">
        <div class="alert alert-info mb-2">
          Showing search results for <strong>{{ accountSearch }}</strong>. Use the search box to find a specific account ID.
        </div>
        <div *ngIf="selectedAccount" class="border rounded p-2 mb-3 bg-light">
          <div class="row">
            <div class="col-sm-4"><strong>ID:</strong> {{ selectedAccount.id || selectedAccount.accountId || selectedAccount.account_id }}</div>
            <div class="col-sm-4"><strong>Holder:</strong> {{ selectedAccount.holderName }}</div>
            <div class="col-sm-2"><strong>Status:</strong> {{ selectedAccount.status }}</div>
            <div class="col-sm-2 text-end"><strong>Balance:</strong> {{ selectedAccount.balance | number:'1.2-2' }}</div>
          </div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-sm mb-0">
          <thead class="table-light">
            <tr>
              <th>Account ID</th>
              <th>Holder</th>
              <th>Status</th>
              <th class="text-end">Balance</th>
              <th class="text-end">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let a of accounts">
              <td>{{ a.accountId || a.account_id || a.id }}</td>
              <td *ngIf="editingId !== (a.accountId || a.account_id || a.id)">{{ a.holderName || a.display_name || a.holder }}</td>
              <td *ngIf="editingId === (a.accountId || a.account_id || a.id)"><input class="form-control form-control-sm" [(ngModel)]="editModel.holderName"/></td>

              <td *ngIf="editingId !== (a.accountId || a.account_id || a.id)">{{ a.status || a.state || '-' }}</td>
              <td *ngIf="editingId === (a.accountId || a.account_id || a.id)">
                <select class="form-select form-select-sm" [(ngModel)]="editModel.status">
                  <option>ACTIVE</option>
                  <option>INACTIVE</option>
                  <option>BLOCKED</option>
                </select>
              </td>

              <td class="text-end">
                <span *ngIf="editingId !== (a.accountId || a.account_id || a.id)">{{ a.balance ?? '-' }}</span>
                <input *ngIf="editingId === (a.accountId || a.account_id || a.id)" type="number" class="form-control form-control-sm" [(ngModel)]="editModel.balance" step="0.01" min="0"/>
              </td>

              <td class="text-end">
                <div *ngIf="editingId !== (a.accountId || a.account_id || a.id)">
                  <button class="btn btn-sm btn-outline-primary me-1" (click)="startEdit(a)">Edit</button>
                  <button class="btn btn-sm btn-outline-danger" (click)="deactivate(a)">Deactivate</button>
                </div>
                <div *ngIf="editingId === (a.accountId || a.account_id || a.id)">
                  <button class="btn btn-sm btn-success me-1" (click)="saveEdit(a)" [disabled]="savingEdit">
                    <span *ngIf="!savingEdit">Save</span>
                    <span *ngIf="savingEdit"><span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Saving...</span>
                  </button>
                  <button class="btn btn-sm btn-secondary" (click)="cancelEdit()" [disabled]="savingEdit">Cancel</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="card-footer bg-light d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
        <div class="text-muted small">
          Showing {{ accountCurrentPage * accountPageSize + 1 }} to {{ accountCurrentPage * accountPageSize + (accounts?.length || 0) }} of {{ accountTotalElements }} accounts
        </div>
        <div class="d-flex gap-2 align-items-center">
          <button class="btn btn-sm btn-outline-primary" (click)="accountPrevPage()" [disabled]="accountCurrentPage===0">← Prev</button>
          <span class="badge bg-secondary">Page {{ accountCurrentPage + 1 }} of {{ accountTotalPages }}</span>
          <button class="btn btn-sm btn-outline-primary" (click)="accountNextPage()" [disabled]="accountCurrentPage>=accountTotalPages-1">Next →</button>
        </div>
      </div>
    </div>

    <div *ngIf="loadingTxns" class="alert alert-info py-2">Loading transactions…</div>
    <div *ngIf="txnsError" class="alert alert-danger py-2">{{ txnsError }}</div>

    <div class="card" *ngIf="!loadingTxns">
      <div class="card-header d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2">
        <div>All Transactions</div>
        <div class="input-group input-group-sm w-100 w-md-auto">
          <input type="text" class="form-control" placeholder="Search transaction by ID"
                 [(ngModel)]="transactionSearch" (keyup.enter)="searchTransactions()" />
          <button class="btn btn-outline-secondary" type="button" (click)="searchTransactions()">Search</button>
        </div>
      </div>
      <div *ngIf="transactionSearch?.trim()" class="card-body py-2">
        <div class="alert alert-info mb-2">
          Showing search results for <strong>{{ transactionSearch }}</strong>. Use the search box to find a specific transaction ID.
        </div>
        <div *ngIf="selectedTransaction" class="border rounded p-2 mb-3 bg-light">
          <div class="row">
            <div class="col-sm-2"><strong>ID:</strong> {{ selectedTransaction.transactionId }}</div>
            <div class="col-sm-3"><strong>From:</strong> {{ selectedTransaction.fromAccountId }}</div>
            <div class="col-sm-3"><strong>To:</strong> {{ selectedTransaction.toAccountId }}</div>
            <div class="col-sm-2"><strong>Amount:</strong> {{ getAmountDisplay(selectedTransaction) }}</div>
            <div class="col-sm-2"><strong>Status:</strong> <span class="badge" [ngClass]="statusBadgeClass(selectedTransaction.status)">{{ selectedTransaction.status }}</span></div>
          </div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>From</th>
              <th>To</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Created On</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngIf="!displayedTxns.length">
              <td colspan="6" class="text-center text-muted py-4">No transactions found</td>
            </tr>
            <tr *ngFor="let t of displayedTxns">
              <td>{{ t.transactionId }}</td>
              <td>{{ t.fromAccountId }}</td>
              <td>{{ t.toAccountId }}</td>
              <td [ngClass]="getAmountClass(t)">{{ getAmountDisplay(t) }}</td>
              <td><span class="badge" [ngClass]="statusBadgeClass(t.status)">{{ t.status }}</span></td>
              <td>{{ t.createdOn | date:'medium' }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card-footer bg-light d-flex justify-content-between align-items-center">
        <div class="text-muted small">Showing {{ getShowingFrom() }} to {{ getShowingTo() }} of {{ totalElements }} transactions</div>
        <div class="d-flex gap-2 align-items-center">
          <button class="btn btn-sm btn-outline-primary" (click)="prevPage()" [disabled]="currentPage===0">← Prev</button>
          <span class="badge bg-secondary">Page {{ currentPage + 1 }} of {{ totalPages }}</span>
          <button class="btn btn-sm btn-outline-primary" (click)="nextPage()" [disabled]="currentPage>=totalPages-1">Next →</button>
        </div>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .amount-credit { color: #28a745; font-weight: 600; }
    .amount-debit { color: #dc3545; font-weight: 600; }
  `]
})
export class AdminDashboardComponent implements OnInit {
  accounts: any[] = [];
  displayedTxns: TransferHistoryItem[] = [];
  txns: TransferHistoryItem[] = [];
  loadingAccounts = false;
  loadingTxns = false;
  accountsError: string | null = null;
  txnsError: string | null = null;

  accountSearch = '';
  accountCurrentPage = 0;
  accountPageSize = 10;
  accountTotalElements = 0;
  accountTotalPages = 0;

  transactionSearch = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  editingId: string | null = null;
  editModel: any = { holderName: '', status: 'ACTIVE', balance: 0 };
  savingEdit = false;

  selectedAccount: any | null = null;
  selectedTransaction: TransferHistoryItem | null = null;

  constructor(private api: BankingApiService) {}

  ngOnInit(): void {
    this.loadAccountsPage(0);
    this.loadTxnsPage(0);
  }

  loadAccountsPage(page: number) {
    this.loadingAccounts = true;
    this.accountsError = null;
    this.accountCurrentPage = page;
    this.api.getAccountsPaginated(page, this.accountPageSize, this.accountSearch).subscribe({
      next: (res) => {
        this.accounts = res.content || [];
        this.accountTotalElements = res.totalElements ?? this.accounts.length;
        this.accountTotalPages = res.totalPages ?? Math.max(1, Math.ceil(this.accountTotalElements / this.accountPageSize));
        this.selectedAccount = (this.accountSearch?.trim() && this.accounts.length > 0) ? this.accounts[0] : null;
        this.loadingAccounts = false;
      },
      error: (e: any) => {
        console.error('Admin accounts load failed', e);
        const status = e?.status ? `Status ${e.status}` : '';
        const msg = e?.error?.message || e?.message || e?.statusText || JSON.stringify(e);
        this.accountsError = (status ? status + ': ' : '') + msg;
        this.loadingAccounts = false;
      }
    });
  }

  startEdit(a: any) {
    const id = a.accountId || a.account_id || a.id;
    this.editingId = id;
    this.editModel = {
      holderName: a.holderName || a.display_name || a.holder || '',
      status: a.status || a.state || 'ACTIVE',
      balance: a.balance ?? 0
    };
  }

  cancelEdit() {
    this.editingId = null;
    this.editModel = { holderName: '', status: 'ACTIVE', balance: 0 };
  }

  saveEdit(a: any) {
    const id = a.accountId || a.account_id || a.id;
    const payload: any = {
      holderName: this.editModel.holderName,
      status: this.editModel.status
    };
    
    this.savingEdit = true;
    
    // First, update account details (holderName and status)
    this.api.updateAccount(id, payload).subscribe({
      next: (res) => {
        // If balance changed, update it separately
        if (this.editModel.balance !== undefined && this.editModel.balance !== null) {
              this.api.updateBalance(id, this.editModel.balance).subscribe({
            next: (balRes) => {
              this.savingEdit = false;
              this.cancelEdit();
              this.loadAccountsPage(this.accountCurrentPage);
            },
            error: (e: any) => {
              this.savingEdit = false;
              console.error('Update balance failed', e);
              this.accountsError = (e?.status ? 'Status '+e.status+': ' : '') + (e?.error?.message || e?.message || String(e));
            }
          });
        } else {
          this.savingEdit = false;
          this.cancelEdit();
          this.loadAccountsPage(this.accountCurrentPage);
        }
      },
      error: (e: any) => {
        this.savingEdit = false;
        console.error('Update account failed', e);
        this.accountsError = (e?.status ? 'Status '+e.status+': ' : '') + (e?.error?.message || e?.message || String(e));
      }
    });
  }

  deactivate(a: any) {
    if (!confirm('Deactivate this account?')) return;
    const id = a.accountId || a.account_id || a.id;
    this.api.deactivateAccount(id).subscribe({
      next: () => this.loadAccountsPage(this.accountCurrentPage),
      error: (e:any) => { this.accountsError = (e?.status ? 'Status '+e.status+': ' : '') + (e?.error?.message || e?.message || String(e)); }
    });
  }

  loadTxnsPage(page: number) {
    this.loadingTxns = true;
    this.txnsError = null;
    this.currentPage = page;
    this.api.getAllTransactionsPaginated(page, this.pageSize, this.transactionSearch).subscribe({
      next: (res) => {
        this.txns = res.content || [];
        this.displayedTxns = this.txns;
        this.totalElements = res.totalElements ?? this.txns.length;
        this.totalPages = res.totalPages ?? Math.max(1, Math.ceil(this.totalElements / this.pageSize));
        this.selectedTransaction = (this.transactionSearch?.trim() && this.txns.length > 0) ? this.txns[0] : null;
        this.loadingTxns = false;
      },
      error: (e: any) => {
        console.error('Admin transactions load failed', e);
        const status = e?.status ? `Status ${e.status}` : '';
        const msg = e?.error?.message || e?.message || e?.statusText || JSON.stringify(e);
        this.txnsError = (status ? status + ': ' : '') + msg;
        this.loadingTxns = false;
      }
    });
  }

  nextPage() { if (this.currentPage < this.totalPages - 1) this.loadTxnsPage(this.currentPage + 1); }
  prevPage() { if (this.currentPage > 0) this.loadTxnsPage(this.currentPage - 1); }

  accountNextPage() { if (this.accountCurrentPage < this.accountTotalPages - 1) this.loadAccountsPage(this.accountCurrentPage + 1); }
  accountPrevPage() { if (this.accountCurrentPage > 0) this.loadAccountsPage(this.accountCurrentPage - 1); }

  searchAccounts() { this.loadAccountsPage(0); }
  searchTransactions() { this.loadTxnsPage(0); }

  getShowingFrom() { return this.totalElements ? (this.currentPage * this.pageSize) + 1 : 0; }
  getShowingTo() { return this.totalElements ? (this.currentPage * this.pageSize) + (this.displayedTxns?.length || 0) : 0; }

  isCredit(t: TransferHistoryItem) { return t.toAccountId === (t.toAccountId || ''); }
  getAmountDisplay(t: TransferHistoryItem) {
    const sign = this.isCredit(t) ? '+ ' : '- ';
    const formatted = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 2 }).format(t.amount);
    return sign + formatted;
  }
  getAmountClass(t: TransferHistoryItem) { return this.isCredit(t) ? 'amount-credit' : 'amount-debit'; }
  statusBadgeClass(s?: string) { const st = (s||'').toUpperCase(); if (st==='SUCCESS') return 'text-bg-success'; if (st==='PENDING') return 'text-bg-warning'; return 'text-bg-danger'; }
}
