import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BankingApiService, TransferResponseDTO } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-transfer-money',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="m-0">Transfer Money</h3>
      <a class="btn btn-light btn-sm" [routerLink]="['/profile']">← Back to Profile</a>
    </div>

    <form #f="ngForm" (ngSubmit)="submit(f)" class="card p-3 shadow-soft">
      <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label">From Account ID</label>
            <input class="form-control"
                   type="text"
                   name="fromAccountId"
                   [(ngModel)]="fromAccountId"
                   required />
          </div>

        <div class="col-md-4">
          <label class="form-label">To Account ID</label>
          <input class="form-control"
                 type="text"
                 name="toAccountId"
                 [(ngModel)]="toAccountId"
                 required />
        </div>

        <div class="col-md-4">
          <label class="form-label">Amount</label>
          <input class="form-control"
                 type="number"
                 name="amount"
                 [(ngModel)]="amount"
                 step="0.01"
                 required
                 min="0.01" />
        </div>
      </div>

      <div class="mt-3 d-flex gap-2 align-items-center">
        <button class="btn btn-primary" type="submit" [disabled]="f.invalid || loading">
          {{ loading ? 'Sending…' : 'Send' }}
        </button>

        <a class="btn btn-outline-secondary"
           [routerLink]="['/transactions']"
           [queryParams]="{ accountId: fromAccountId }">
          View History
        </a>
      </div>

      <div *ngIf="errorMsg" class="alert alert-danger mt-3">{{ errorMsg }}</div>
      <div *ngIf="successMsg" class="alert alert-success mt-3">
        <div class="fw-semibold mb-1">Transfer initiated</div>
        <div>Transaction ID: {{ lastResponse?.transactionId }}</div>
        <div>Status: {{ lastResponse?.status }}</div>
        <div class="text-muted">{{ lastResponse?.message }}</div>
      </div>
    </form>
  </div>
  `,
})
export class TransferMoneyComponent {
  constructor(
    private api: BankingApiService,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {
    // Prefer query param; fallback to logged-in user's account id; then empty string as last resort
    const qp = this.route.snapshot.queryParamMap.get('fromAccountId') ?? '';
    const authId = this.auth.userId ?? '';
    this.fromAccountId = qp || authId || '';
  }

  fromAccountId = '';
  toAccountId!: string;
  amount!: number;
  idempotencyKey = '';

  loading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;
  lastResponse: TransferResponseDTO | null = null;

  submit(f: NgForm) {
    if (f.invalid) return;

    // Basic validations
    if (!this.fromAccountId || !this.toAccountId || !Number.isFinite(this.amount)) {
      this.errorMsg = 'Please provide valid values for all fields.';
      return;
    }
    if (this.fromAccountId === this.toAccountId) {
      this.errorMsg = 'From and To accounts cannot be the same.';
      return;
    }
    if (this.amount <= 0) {
      this.errorMsg = 'Amount must be greater than 0.';
      return;
    }

    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;
    this.lastResponse = null;

    // Verify payee account exists before attempting transfer
    this.api.accountExists(this.toAccountId).subscribe({
      next: (exists) => {
        if (!exists) {
          this.errorMsg = 'Payee account does not exist.';
          this.loading = false;
          return;
        }

        // proceed with transfer
        this.api.transfer(this.fromAccountId, this.toAccountId, this.amount, this.idempotencyKey || undefined)
          .subscribe({
            next: (res) => {
              this.lastResponse = res;
              const status = (res?.status || '').toUpperCase();
              if (status === 'SUCCESS') {
                this.successMsg = 'Transfer request accepted.';
                this.errorMsg = null;
              } else {
                this.errorMsg = res?.message || 'Transfer failed';
                this.successMsg = null;
              }
              this.loading = false;
            },
            error: (e: Error) => {
              this.errorMsg = e.message || 'Transfer failed';
              this.loading = false;
            }
          });
      },
      error: (e: Error) => {
        this.errorMsg = e.message || 'Failed to verify payee account';
        this.loading = false;
      }
    });
  }
}