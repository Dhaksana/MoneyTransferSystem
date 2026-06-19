import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <section class="notifications-page">
      <div><p class="eyebrow">Alerts</p><h1>Notifications</h1></div>
      <mat-card class="glass-card" *ngFor="let item of notifications.notifications$ | async">
        <mat-card-content class="row">
          <mat-icon>{{ item.read ? 'notifications_none' : 'notifications_active' }}</mat-icon>
          <div><strong>{{ item.type }}</strong><p>{{ item.message }}</p><span>{{ item.createdAt | date:'medium' }}</span></div>
          <button mat-button (click)="notifications.markRead(item)">Mark read</button>
          <button mat-icon-button color="warn" (click)="notifications.delete(item)"><mat-icon>delete</mat-icon></button>
        </mat-card-content>
      </mat-card>
    </section>
  `,
  styles: [`
    .notifications-page { display:grid; gap:14px; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .glass-card { border-radius:18px; background:rgba(255,255,255,.84); }
    .row { display:grid; grid-template-columns:40px 1fr auto auto; gap:12px; align-items:center; }
    p { margin:3px 0; color:#334155; } span { color:#64748b; font-size:12px; } mat-icon { color:#0f766e; }
  `]
})
export class NotificationsComponent {
  constructor(public notifications: NotificationService) {}
}
