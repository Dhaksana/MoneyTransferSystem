import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { BankingApiService, RewardHistory, RewardSummary } from '../services/banking-api.service';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTableModule],
  template: `
    <section class="rewards-page">
      <div class="page-heading"><p class="eyebrow">Loyalty</p><h1>Reward points</h1></div>
      <div class="kpi-grid">
        <mat-card class="glass-card hero"><mat-card-content><mat-icon>workspace_premium</mat-icon><span>Current points</span><strong>{{ summary.currentPoints }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Lifetime points</span><strong>{{ summary.lifetimePoints }}</strong><p>Earn 1 point for every ₹100 in eligible transfers.</p></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Monthly points</span><strong>{{ monthlyPoints }}</strong><p>Points earned this month.</p></mat-card-content></mat-card>
      </div>
      <mat-card class="glass-card">
        <mat-card-header><mat-card-title>Reward history</mat-card-title></mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="history">
            <ng-container matColumnDef="transactionId"><th mat-header-cell *matHeaderCellDef>Transaction</th><td mat-cell *matCellDef="let r">#{{ r.transactionId }}</td></ng-container>
            <ng-container matColumnDef="pointsEarned"><th mat-header-cell *matHeaderCellDef>Points</th><td mat-cell *matCellDef="let r">{{ r.pointsEarned }}</td></ng-container>
            <ng-container matColumnDef="reason"><th mat-header-cell *matHeaderCellDef>Reason</th><td mat-cell *matCellDef="let r">{{ r.reason }}</td></ng-container>
            <ng-container matColumnDef="createdAt"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let r">{{ r.createdAt | date:'medium' }}</td></ng-container>
            <tr mat-header-row *matHeaderRowDef="columns"></tr><tr mat-row *matRowDef="let row; columns: columns;"></tr>
          </table>
        </mat-card-content>
      </mat-card>
    </section>
  `,
  styles: [`
    .rewards-page { display:grid; gap:22px; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .kpi-grid { display:grid; grid-template-columns:repeat(3, minmax(220px, 1fr)); gap:16px; }
    .glass-card { border-radius:22px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); border:1px solid rgba(255,255,255,.68); }
    .hero { background:linear-gradient(135deg,#0f2742,#0f766e); color:white; }
    mat-card-content { display:grid; gap:8px; }
    strong { font-size:36px; color:#0f2742; }
    .hero strong { color:white; }
    mat-icon { color:#2dd4bf; }
    table { width:100%; }
  `]
})
export class RewardsComponent implements OnInit {
  summary: RewardSummary = { currentPoints: 0, lifetimePoints: 0 };
  history: RewardHistory[] = [];
  columns = ['transactionId', 'pointsEarned', 'reason', 'createdAt'];
  get monthlyPoints() {
    const now = new Date();
    return this.history.filter(h => {
      const d = new Date(h.createdAt);
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    }).reduce((sum, h) => sum + h.pointsEarned, 0);
  }
  constructor(private api: BankingApiService) {}
  ngOnInit() {
    this.api.getRewardSummary().subscribe(s => this.summary = s);
    this.api.getRewardHistory().subscribe(h => this.history = h);
  }
}
