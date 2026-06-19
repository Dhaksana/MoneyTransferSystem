import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BankingApiService } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-statements',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule],
  template: `
    <section class="doc-page">
      <div><p class="eyebrow">Documents</p><h1>Account statements</h1></div>
      <mat-card class="glass-card">
        <mat-card-content class="form-grid">
          <mat-form-field appearance="outline"><mat-label>Account number</mat-label><input matInput [(ngModel)]="accountId" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>From</mat-label><input matInput type="date" [(ngModel)]="from" /></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>To</mat-label><input matInput type="date" [(ngModel)]="to" /></mat-form-field>
          <button mat-flat-button color="primary" (click)="download()" [disabled]="loading || !accountId || !from || !to">
            <mat-spinner *ngIf="loading" diameter="18"></mat-spinner><mat-icon *ngIf="!loading">picture_as_pdf</mat-icon>Download PDF
          </button>
        </mat-card-content>
      </mat-card>
    </section>
  `,
  styles: [`
    .doc-page { display:grid; gap:22px; max-width:900px; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .glass-card { border-radius:22px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); border:1px solid rgba(255,255,255,.68); }
    .form-grid { display:grid; grid-template-columns:2fr 1fr 1fr auto; gap:14px; align-items:center; }
    button { height:56px; border-radius:16px; }
  `]
})
export class StatementsComponent {
  accountId = this.auth.userId || '';
  from = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10);
  to = new Date().toISOString().slice(0, 10);
  loading = false;
  constructor(private api: BankingApiService, private auth: AuthService) {}
  download() {
    this.loading = true;
    this.api.downloadStatement(this.accountId, this.from, this.to).subscribe(blob => {
      this.api.saveBlob(blob, `statement-${this.accountId}.pdf`);
      this.loading = false;
    });
  }
}
