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
                 type="number"
                 name="fromAccountId"
                 [(ngModel)]="fromAccountId"
                 required
                 min="1" />
        </div>

        <div class="col-md-4">
          <label class="form-label">To Account ID</label>
          <input class="form-control"
                 type="number"
                 name="toAccountId"
                 [(ngModel)]="toAccountId"
                 required
                 min="1" />
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

      <div class="row g-3 mt-1">
        <div class="col-md-6">
          <label class="form-label">Idempotency Key (optional)</label>
          <input class="form-control"
                 type="text"
                 name="idempotencyKey"
                 [(ngModel)]="idempotencyKey"
                 placeholder="Leave empty to auto-generate" />
          <div class="form-text">Use the same key to safely retry without duplicating the transaction.</div>
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
    // Prefer query param; fallback to logged-in user's account id; then 1 as last resort
    const qp = Number(this.route.snapshot.queryParamMap.get('fromAccountId') ?? 'NaN');
    const authId = this.auth.userId ?? NaN;
    this.fromAccountId = Number.isFinite(qp) ? qp : (Number.isFinite(authId) ? authId : 1);
  }

  fromAccountId = 1;
  toAccountId!: number;
  amount!: number;
  idempotencyKey = '';

  loading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;
  lastResponse: TransferResponseDTO | null = null;

  submit(f: NgForm) {
    if (f.invalid) return;

    // Basic validations
    if (!Number.isFinite(this.fromAccountId) || !Number.isFinite(this.toAccountId) || !Number.isFinite(this.amount)) {
      this.errorMsg = 'Please provide valid numbers for all fields.';
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

    this.api.transfer(this.fromAccountId, this.toAccountId, this.amount, this.idempotencyKey || undefined)
      .subscribe({
        next: (res) => {
          this.lastResponse = res;
          this.successMsg = 'Transfer request accepted.';
          this.loading = false;
        },
        error: (e: Error) => {
          this.errorMsg = e.message || 'Transfer failed';
          this.loading = false;
        }
      });
  }
}