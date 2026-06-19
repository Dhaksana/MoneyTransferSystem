import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { BankingApiService, AnalyticsSummary } from '../services/banking-api.service';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <section class="analytics-page">
      <div class="page-heading"><p class="eyebrow">Insights</p><h1>Banking analytics</h1></div>
      <div class="kpi-grid" *ngIf="summary">
        <mat-card class="glass-card"><mat-card-content><span>Sent this month</span><strong>{{ summary.moneySentThisMonth | currency:'INR':'symbol':'1.0-0' }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Received this month</span><strong>{{ summary.moneyReceivedThisMonth | currency:'INR':'symbol':'1.0-0' }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Transactions</span><strong>{{ summary.transactionCount }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Largest transaction</span><strong>{{ summary.largestTransaction | currency:'INR':'symbol':'1.0-0' }}</strong></mat-card-content></mat-card>
      </div>
      <div class="chart-grid">
        <mat-card class="glass-card"><mat-card-header><mat-card-title>Sent vs received</mat-card-title></mat-card-header><mat-card-content><canvas #trendCanvas></canvas></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-header><mat-card-title>Status distribution</mat-card-title></mat-card-header><mat-card-content><canvas #statusCanvas></canvas></mat-card-content></mat-card>
      </div>
    </section>
  `,
  styles: [`
    .analytics-page { display:grid; gap:22px; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .kpi-grid { display:grid; grid-template-columns:repeat(4,minmax(180px,1fr)); gap:16px; }
    .chart-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
    .glass-card { border-radius:22px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); border:1px solid rgba(255,255,255,.68); }
    strong { font-size:26px; color:#0f2742; }
    span { color:#64748b; font-weight:700; }
    canvas { max-height:320px; }
  `]
})
export class AnalyticsComponent implements OnInit, AfterViewInit {
  @ViewChild('trendCanvas') trendCanvas?: ElementRef<HTMLCanvasElement>;
  @ViewChild('statusCanvas') statusCanvas?: ElementRef<HTMLCanvasElement>;
  summary: AnalyticsSummary | null = null;
  private viewReady = false;
  constructor(private api: BankingApiService) {}
  ngOnInit() {
    this.api.getAnalytics().subscribe(summary => {
      this.summary = summary;
      this.renderCharts();
    });
  }
  ngAfterViewInit() {
    this.viewReady = true;
    this.renderCharts();
  }
  private renderCharts() {
    if (!this.viewReady || !this.summary || !this.trendCanvas || !this.statusCanvas) return;
    new Chart(this.trendCanvas.nativeElement, {
      type: 'bar',
      data: { labels: this.summary.monthlyTransactionTrend.map(p => p.label), datasets: [{ data: this.summary.monthlyTransactionTrend.map(p => p.value), backgroundColor: ['#0f766e', '#0f2742'] }] }
    });
    new Chart(this.statusCanvas.nativeElement, {
      type: 'doughnut',
      data: { labels: this.summary.statusDistribution.map(p => p.label), datasets: [{ data: this.summary.statusDistribution.map(p => p.value), backgroundColor: ['#14b8a6', '#ef4444'] }] }
    });
  }
}
