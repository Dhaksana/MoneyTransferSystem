import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BankingApiService, TransferResponseDTO } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';

interface ConfirmTransferData {
  from: string;
  to: string;
  amount: number;
}

@Component({
  selector: 'app-confirm-transfer-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>Confirm transfer</h2>
    <mat-dialog-content class="confirm-body">
      <mat-icon>verified</mat-icon>
      <p>Send <strong>{{ data.amount | currency:'INR':'symbol':'1.0-2' }}</strong> from {{ data.from }} to {{ data.to }}?</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="true">Confirm</button>
    </mat-dialog-actions>
  `,
  styles: [`.confirm-body { display: flex; align-items: center; gap: 16px; color: #52525b; } mat-icon { color: #0d9488; }`]
})
export class ConfirmTransferDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmTransferData) {}
}

@Component({
  selector: 'app-transfer-money',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <section class="transfer-page">
      <div class="page-heading">
        <div>
          <p class="eyebrow">Payments</p>
          <h1>Transfer money</h1>
        </div>
        <a mat-stroked-button routerLink="/transactions"><mat-icon>receipt_long</mat-icon>History</a>
      </div>

      <mat-card class="glass-card transfer-card">
        <mat-card-content>
          <form #f="ngForm" (ngSubmit)="submit(f)" class="transfer-form">
            <mat-form-field appearance="outline">
              <mat-label>From account number</mat-label>
              <input matInput name="fromAccountNumber" [(ngModel)]="fromAccountNumber" required />
              <mat-icon matSuffix>account_balance</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Recipient account number</mat-label>
              <input matInput name="toAccountNumber" [(ngModel)]="toAccountNumber" required (blur)="previewRecipient()" />
              <mat-icon matSuffix>person_search</mat-icon>
            </mat-form-field>

            <div class="recipient-preview" [class.valid]="recipientExists" [class.invalid]="recipientExists === false">
              <mat-icon>{{ recipientExists ? 'check_circle' : recipientExists === false ? 'error' : 'manage_search' }}</mat-icon>
              <span>{{ recipientPreview }}</span>
            </div>

            <mat-form-field appearance="outline">
              <mat-label>Amount</mat-label>
              <input matInput type="number" name="amount" [(ngModel)]="amount" min="0.01" step="0.01" required />
              <span matTextPrefix>₹&nbsp;</span>
            </mat-form-field>

            <button mat-flat-button color="primary" type="submit" [disabled]="f.invalid || loading">
              <mat-spinner *ngIf="loading" diameter="18"></mat-spinner>
              <mat-icon *ngIf="!loading">send</mat-icon>
              {{ loading ? 'Processing' : 'Review transfer' }}
            </button>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card class="glass-card result-card" *ngIf="lastResponse">
        <mat-icon [class.success]="lastResponse.status === 'SUCCESS'">{{ lastResponse.status === 'SUCCESS' ? 'task_alt' : 'cancel' }}</mat-icon>
        <div>
          <strong>{{ lastResponse.status }}</strong>
          <span>{{ lastResponse.message }}</span>
        </div>
      </mat-card>
    </section>
  `,
  styles: [`
    .transfer-page { display: grid; gap: 22px; max-width: 900px; }
    .page-heading { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .eyebrow { margin: 0 0 6px; color: #0d9488; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; }
    h1 { margin: 0; color: #18181b; font: 700 32px/1.15 Poppins, Inter, sans-serif; }
    .glass-card { border-radius: 12px; background: white; box-shadow: 0 2px 12px rgba(0,0,0,.06); border: 1px solid #e4e4e7; }
    .transfer-card { padding: 10px; }
    .transfer-form { display: grid; gap: 16px; }
    .recipient-preview { display: flex; align-items: center; gap: 10px; min-height: 48px; padding: 0 14px; border-radius: 12px; background: #f4f4f5; color: #71717a; }
    .recipient-preview.valid { background: #dcfce7; color: #166534; }
    .recipient-preview.invalid { background: #fee2e2; color: #991b1b; }
    button[type="submit"] { height: 52px; border-radius: 12px; }
    button mat-spinner { margin-right: 8px; }
    .result-card { display: flex; flex-direction: row; align-items: center; gap: 14px; padding: 18px; animation: rise .22s ease-out; }
    .result-card mat-icon { color: #dc2626; }
    .result-card mat-icon.success { color: #0d9488; }
    .result-card div { display: grid; }
    .result-card span { color: #71717a; }
    @keyframes rise { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: none; } }
  `],
})
export class TransferMoneyComponent {
  constructor(
    private api: BankingApiService,
    private route: ActivatedRoute,
    private auth: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    const qp = this.route.snapshot.queryParamMap.get('fromAccountId') ?? '';
    const to = this.route.snapshot.queryParamMap.get('toAccountNumber') ?? '';
    const authId = this.auth.userId ?? '';
    this.fromAccountNumber = qp || authId || '';
    this.toAccountNumber = to;
  }

  fromAccountNumber = '';
  toAccountNumber = '';
  amount!: number;
  idempotencyKey = '';
  recipientExists: boolean | null = null;
  recipientPreview = 'Enter a recipient account number to preview availability.';
  loading = false;
  lastResponse: TransferResponseDTO | null = null;

  previewRecipient() {
    if (!this.toAccountNumber?.trim()) {
      this.recipientExists = null;
      this.recipientPreview = 'Enter a recipient account number to preview availability.';
      return;
    }
    this.api.accountNumberExists(this.toAccountNumber).subscribe({
      next: (exists) => {
        this.recipientExists = exists;
        this.recipientPreview = exists ? 'Recipient account found.' : 'No account found for this number.';
      },
      error: (e: Error) => {
        this.recipientExists = false;
        this.recipientPreview = e.message;
      }
    });
  }

  submit(f: NgForm) {
    if (f.invalid) return;
    if (this.fromAccountNumber === this.toAccountNumber) {
      this.snackBar.open('From and recipient accounts cannot match.', 'Close', { duration: 3200 });
      return;
    }
    if (!Number.isFinite(this.amount) || this.amount <= 0) {
      this.snackBar.open('Enter a valid transfer amount.', 'Close', { duration: 3200 });
      return;
    }

    const ref = this.dialog.open(ConfirmTransferDialogComponent, {
      width: '420px',
      data: { from: this.fromAccountNumber, to: this.toAccountNumber, amount: this.amount }
    });

    ref.afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      this.loading = true;
      this.lastResponse = null;
      this.api.transfer(this.fromAccountNumber, this.toAccountNumber, this.amount, this.idempotencyKey || undefined, true)
        .subscribe({
          next: (res) => {
            this.lastResponse = res;
            this.loading = false;
            this.snackBar.open(res.status === 'SUCCESS' ? 'Transfer completed.' : res.message, 'Close', { duration: 3600 });
          },
          error: (e: Error) => {
            this.loading = false;
            this.snackBar.open(e.message || 'Transfer failed', 'Close', { duration: 4200 });
          }
        });
    });
  }
}
