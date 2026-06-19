import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { BankingApiService } from '../services/banking-api.service';
import { TransferHistoryItem } from '../models/transfer.model';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';

type Filter = 'all' | 'received' | 'sent' | 'success' | 'failure';

@Component({
  selector: 'app-transaction-history',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule
  ],
  template: `
    <section class="history-page">
      <div class="page-heading">
        <div>
          <p class="eyebrow">Ledger</p>
          <h1>Transaction history</h1>
          <span>Account {{ accountId }}</span>
        </div>
        <a mat-flat-button color="primary" routerLink="/transfer"><mat-icon>swap_horiz</mat-icon>New transfer</a>
      </div>

      <mat-card class="glass-card">
        <mat-card-content>
          <div class="table-tools">
            <mat-form-field appearance="outline">
              <mat-label>Search transactions</mat-label>
              <input matInput (keyup)="applySearch($event)" placeholder="Account, status, amount" />
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Filter</mat-label>
              <mat-select [(value)]="selected" (selectionChange)="applyFilter()">
                <mat-option value="all">All</mat-option>
                <mat-option value="received">Received</mat-option>
                <mat-option value="sent">Sent</mat-option>
                <mat-option value="success">Success</mat-option>
                <mat-option value="failure">Failed</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <div *ngIf="loading" class="empty-state">Loading transactions...</div>
          <div *ngIf="errorMsg" class="error-state">{{ errorMsg }}</div>

          <div class="table-wrap" *ngIf="!loading">
            <table mat-table [dataSource]="dataSource" matSort>
              <ng-container matColumnDef="transactionId">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Reference</th>
                <td mat-cell *matCellDef="let txn">#{{ txn.transactionId || '-' }}</td>
              </ng-container>

              <ng-container matColumnDef="fromAccountId">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>From</th>
                <td mat-cell *matCellDef="let txn">{{ txn.fromAccountId }}</td>
              </ng-container>

              <ng-container matColumnDef="toAccountId">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>To</th>
                <td mat-cell *matCellDef="let txn">{{ txn.toAccountId }}</td>
              </ng-container>

              <ng-container matColumnDef="amount">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Amount</th>
                <td mat-cell *matCellDef="let txn" class="amount">{{ txn.amount | currency:'INR':'symbol':'1.0-2' }}</td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
                <td mat-cell *matCellDef="let txn">
                  <span class="status-pill" [class.failed]="txn.status === 'FAILED'" [class.pending]="txn.status === 'PENDING'">{{ txn.status }}</span>
                </td>
              </ng-container>

              <ng-container matColumnDef="failureReason">
                <th mat-header-cell *matHeaderCellDef>Notes</th>
                <td mat-cell *matCellDef="let txn">{{ txn.failureReason || '-' }}</td>
              </ng-container>

              <ng-container matColumnDef="createdOn">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Date</th>
                <td mat-cell *matCellDef="let txn">{{ txn.createdOn | date:'medium' }}</td>
              </ng-container>

              <ng-container matColumnDef="receipt">
                <th mat-header-cell *matHeaderCellDef>Receipt</th>
                <td mat-cell *matCellDef="let txn">
                  <button mat-icon-button color="primary" (click)="downloadReceipt(txn)" [disabled]="!txn.transactionId">
                    <mat-icon>picture_as_pdf</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
            <div *ngIf="!dataSource.filteredData.length" class="empty-state">No matching transactions found.</div>
            <mat-paginator [pageSize]="8" [pageSizeOptions]="[5, 8, 15]" showFirstLastButtons></mat-paginator>
          </div>
        </mat-card-content>
      </mat-card>
    </section>
  `,
  styles: [`
    .history-page { display: grid; gap: 22px; }
    .page-heading { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .page-heading span { color: #71717a; }
    .eyebrow { margin: 0 0 6px; color: #0d9488; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; }
    h1 { margin: 0; color: #18181b; font: 700 32px/1.15 Poppins, Inter, sans-serif; }
    .glass-card { border-radius: 12px; background: white; box-shadow: 0 2px 12px rgba(0,0,0,.06); border: 1px solid #e4e4e7; }
    .table-tools { display: grid; grid-template-columns: 1fr 220px; gap: 14px; }
    .table-wrap { overflow: auto; border-radius: 12px; border: 1px solid #e4e4e7; }
    table { width: 100%; background: white; }
    th { color: #27272a; font-weight: 800; }
    .amount { font-weight: 800; color: #18181b; }
    .status-pill { padding: 5px 10px; border-radius: 999px; background: #dcfce7; color: #166534; font-weight: 800; font-size: 12px; }
    .status-pill.failed { background: #fee2e2; color: #991b1b; }
    .status-pill.pending { background: #fef3c7; color: #92400e; }
    .empty-state, .error-state { padding: 18px; color: #71717a; }
    .error-state { color: #dc2626; }
    @media (max-width: 760px) { .table-tools { grid-template-columns: 1fr; } .page-heading { align-items: flex-start; flex-direction: column; } }
  `],
})
export class TransactionHistoryComponent implements OnInit, AfterViewInit {
  constructor(
    private api: BankingApiService,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  @ViewChild(MatPaginator) paginator?: MatPaginator;
  @ViewChild(MatSort) sort?: MatSort;

  accountId = '';
  transactions: TransferHistoryItem[] = [];
  loading = false;
  errorMsg: string | null = null;
  selected: Filter = 'all';
  displayedColumns = ['transactionId', 'fromAccountId', 'toAccountId', 'amount', 'status', 'failureReason', 'createdOn', 'receipt'];
  dataSource = new MatTableDataSource<TransferHistoryItem>([]);

  ngOnInit(): void {
    const qpId = this.route.snapshot.queryParamMap.get('accountId') ?? '';
    const authId = this.auth.userId ?? '';
    this.accountId = qpId || authId || '';
    this.dataSource.filterPredicate = (txn, filter) => this.matchesFilter(txn, filter);

    this.loading = true;
    this.api.getHistoryByAccount(this.accountId).subscribe({
      next: (items) => {
        this.transactions = items || [];
        this.dataSource.data = this.transactions;
        this.applyFilter();
        this.loading = false;
      },
      error: (e: { message: string }) => {
        this.errorMsg = e.message || 'Failed to load history';
        this.loading = false;
      },
    });
  }

  ngAfterViewInit(): void {
    if (this.paginator) this.dataSource.paginator = this.paginator;
    if (this.sort) this.dataSource.sort = this.sort;
  }

  applySearch(event: Event) {
    const value = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = `${this.selected}|${value}`;
  }

  applyFilter() {
    const search = this.dataSource.filter.split('|')[1] || '';
    this.dataSource.filter = `${this.selected}|${search}`;
    this.paginator?.firstPage();
  }

  private matchesFilter(txn: TransferHistoryItem, raw: string) {
    const [mode, search] = raw.split('|');
    const upper = (txn.status || '').toUpperCase();
    const byMode =
      mode === 'received' ? txn.toAccountId === this.accountId :
      mode === 'sent' ? txn.fromAccountId === this.accountId :
      mode === 'success' ? upper === 'SUCCESS' :
      mode === 'failure' ? upper === 'FAILED' :
      true;
    const haystack = `${txn.transactionId} ${txn.fromAccountId} ${txn.toAccountId} ${txn.amount} ${txn.status} ${txn.failureReason || ''}`.toLowerCase();
    return byMode && haystack.includes(search || '');
  }

  downloadReceipt(txn: TransferHistoryItem) {
    if (!txn.transactionId) return;
    this.api.downloadReceipt(txn.transactionId).subscribe(blob => {
      this.api.saveBlob(blob, `receipt-${txn.transactionId}.pdf`);
    });
  }
}
