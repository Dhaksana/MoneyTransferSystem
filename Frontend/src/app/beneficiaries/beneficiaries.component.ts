import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { BankingApiService, Beneficiary } from '../services/banking-api.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-beneficiary-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatIconModule],
  template: `
    <h2 mat-dialog-title>{{ model.id ? 'Edit beneficiary' : 'Add beneficiary' }}</h2>
    <mat-dialog-content class="form-grid">
      <mat-form-field appearance="outline"><mat-label>Name</mat-label><input matInput [(ngModel)]="model.beneficiaryName" /></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Account number</mat-label><input matInput [(ngModel)]="model.beneficiaryAccountNumber" /></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Bank</mat-label><input matInput [(ngModel)]="model.bankName" /></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>IFSC</mat-label><input matInput [(ngModel)]="model.ifsc" /></mat-form-field>
      <mat-form-field appearance="outline"><mat-label>Nickname</mat-label><input matInput [(ngModel)]="model.nickname" /></mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-flat-button color="primary" [mat-dialog-close]="model">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`.form-grid { display: grid; gap: 12px; min-width: min(520px, 82vw); }`]
})
export class BeneficiaryDialogComponent {
  model: Beneficiary;
  constructor(@Inject(MAT_DIALOG_DATA) data: Beneficiary) {
    this.model = { ...data };
  }
}

@Component({
  selector: 'app-beneficiaries',
  standalone: true,
  imports: [CommonModule, RouterLink, MatButtonModule, MatCardModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSnackBarModule, MatTableModule],
  template: `
    <section class="beneficiaries-page">
      <div class="page-heading">
        <div><p class="eyebrow">Recipients</p><h1>Beneficiaries</h1></div>
        <button mat-flat-button color="primary" (click)="openDialog()"><mat-icon>person_add</mat-icon>Add beneficiary</button>
      </div>

      <mat-form-field appearance="outline" class="search"><mat-label>Search beneficiaries</mat-label><input matInput (input)="search = $any($event.target).value" /><mat-icon matSuffix>search</mat-icon></mat-form-field>

      <div class="card-grid">
        <mat-card class="glass-card beneficiary-card" *ngFor="let b of filtered()">
          <mat-card-content>
            <button mat-icon-button class="favorite" (click)="toggleFavorite(b)"><mat-icon>{{ b.favorite ? 'star' : 'star_border' }}</mat-icon></button>
            <h3>{{ b.beneficiaryName }}</h3>
            <p>{{ b.beneficiaryAccountNumber }}</p>
            <span>{{ b.bankName || 'Bank' }} {{ b.ifsc ? '- ' + b.ifsc : '' }}</span>
            <div class="actions">
              <a mat-button color="primary" routerLink="/transfer" [queryParams]="{ toAccountNumber: b.beneficiaryAccountNumber }"><mat-icon>send</mat-icon>Transfer</a>
              <button mat-button (click)="openDialog(b)"><mat-icon>edit</mat-icon>Edit</button>
              <button mat-button color="warn" (click)="remove(b)"><mat-icon>delete</mat-icon>Delete</button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .beneficiaries-page { display: grid; gap: 20px; }
    .page-heading { display:flex; justify-content:space-between; align-items:center; gap:16px; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .search { max-width: 520px; }
    .card-grid { display:grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap:16px; }
    .glass-card { border-radius:22px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); border:1px solid rgba(255,255,255,.68); }
    .beneficiary-card { position:relative; }
    .favorite { position:absolute; right:10px; top:10px; color:#0f766e; }
    h3 { margin:0 0 6px; color:#0f2742; }
    p { margin:0; font-weight:800; color:#334155; }
    span { color:#64748b; }
    .actions { display:flex; flex-wrap:wrap; gap:4px; margin-top:16px; }
  `]
})
export class BeneficiariesComponent implements OnInit {
  beneficiaries: Beneficiary[] = [];
  search = '';

  constructor(private api: BankingApiService, private auth: AuthService, private dialog: MatDialog, private snack: MatSnackBar) {}

  ngOnInit() { this.load(); }

  load() {
    this.api.getBeneficiaries().subscribe(items => this.beneficiaries = items);
  }

  filtered() {
    const q = this.search.toLowerCase();
    return this.beneficiaries.filter(b => `${b.beneficiaryName} ${b.beneficiaryAccountNumber} ${b.bankName || ''}`.toLowerCase().includes(q));
  }

  openDialog(beneficiary?: Beneficiary) {
    const model = beneficiary || { ownerAccountId: this.auth.userId || '', beneficiaryName: '', beneficiaryAccountNumber: '', favorite: false };
    this.dialog.open(BeneficiaryDialogComponent, { data: model }).afterClosed().subscribe((result?: Beneficiary) => {
      if (!result) return;
      result.ownerAccountId = result.ownerAccountId || this.auth.userId || '';
      this.api.saveBeneficiary(result).subscribe(() => { this.snack.open('Beneficiary saved', 'Close', { duration: 2400 }); this.load(); });
    });
  }

  toggleFavorite(beneficiary: Beneficiary) {
    this.api.saveBeneficiary({ ...beneficiary, favorite: !beneficiary.favorite }).subscribe(() => this.load());
  }

  remove(beneficiary: Beneficiary) {
    if (!beneficiary.id) return;
    this.api.deleteBeneficiary(beneficiary.id).subscribe(() => { this.snack.open('Beneficiary deleted', 'Close', { duration: 2400 }); this.load(); });
  }
}
