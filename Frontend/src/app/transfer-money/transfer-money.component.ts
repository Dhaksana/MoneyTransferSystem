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
          <label class="form-label">To Account ID <span class="text-danger">*</span></label>
          <div class="input-group">
            <input class="form-control"
                   type="text"
                   name="toAccountId"
                   [(ngModel)]="toAccountId"
                   (blur)="verifyToAccount()"
                   (change)="verifyToAccount()"
                   required />
            <button class="btn btn-outline-secondary" type="button" (click)="verifyToAccount()" [disabled]="verifyingAccount">
              {{ verifyingAccount ? 'Verifying...' : 'Verify' }}
            </button>
          </div>
          <small *ngIf="toAccountVerified" class="text-success d-block mt-1">
            {{ toAccountHolderName }}
          </small>
          <small *ngIf="!toAccountVerified && toAccountId" class="text-danger d-block mt-1">
            Account not verified
          </small>
        </div>

        <div class="col-md-4">
          <label class="form-label">Amount</label>
          <input class="form-control amount-input"
                 type="number"
                 name="amount"
                 [(ngModel)]="amount"
                 step="0.01"
                 min="0.01"
                 [disabled]="!toAccountVerified"
                 required />
          <small *ngIf="!toAccountVerified" class="text-muted d-block mt-1">
            Verify account to enter amount
          </small>
        </div>
      </div>

      <div class="mt-3 d-flex gap-2 align-items-center">
        <button class="btn btn-primary" type="submit" [disabled]="f.invalid || loading || !toAccountVerified">
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
  styleUrls: ['./transfer-money.component.css'],
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
  toAccountHolderName: string | null = null;
  toAccountVerified = false;
  verifyingAccount = false;
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

    // Account must be verified before transfer
    if (!this.toAccountVerified) {
      this.errorMsg = 'Please verify the recipient account first.';
      return;
    }

    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;
    this.lastResponse = null;

    // Proceed directly with transfer since account is already verified
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

  // Verify the To Account ID and fetch holder name
  verifyToAccount() {
    if (!this.toAccountId || this.toAccountId.trim() === '') {
      this.toAccountVerified = false;
      this.toAccountHolderName = null;
      this.errorMsg = null;
      return;
    }

    this.verifyingAccount = true;
    this.errorMsg = null;

    this.api.getAccountById(this.toAccountId).subscribe({
      next: (account: any) => {
        if (account && account.holderName) {
          this.toAccountVerified = true;
          this.toAccountHolderName = account.holderName;
        } else {
          this.toAccountVerified = false;
          this.toAccountHolderName = null;
          this.errorMsg = 'Account not found';
        }
        this.verifyingAccount = false;
      },
      error: (e: Error) => {
        this.toAccountVerified = false;
        this.toAccountHolderName = null;
        this.errorMsg = e.message || 'Failed to verify account';
        this.verifyingAccount = false;
      }
    });
  }
}