import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
    selector: 'app-root',
    imports: [
        CommonModule,
        RouterOutlet,
        RouterLink,
        RouterLinkActive,
        MatButtonModule,
        MatBadgeModule,
        MatIconModule,
        MatMenuModule,
        MatSidenavModule,
        MatToolbarModule,
        MatTooltipModule
    ],
    templateUrl: './app.component.html',
    changeDetection: ChangeDetectionStrategy.Default,
    styleUrls: ['./app.component.css']
})
export class AppComponent {
  auth = inject(AuthService);
  notifications = inject(NotificationService);
  router = inject(Router);
  collapsed = false;

  constructor() {
    console.log('[MTS] AppComponent constructed');
    if (this.auth.role === 'ADMIN') {
      this.collapsed = true;
    }
  }

  get navItems() {
    const isAdmin = this.auth.role === 'ADMIN';
    if (isAdmin) {
      return [
        { label: 'Admin', route: '/admin', icon: 'admin_panel_settings' }
      ];
    }
    return [
      { label: 'Dashboard', route: '/profile', icon: 'dashboard' },
      { label: 'Transfer', route: '/transfer', icon: 'swap_horiz' },
      { label: 'Transactions', route: '/transactions', icon: 'receipt_long' },
      { label: 'Statements', route: '/statements', icon: 'description' },
      { label: 'Beneficiaries', route: '/beneficiaries', icon: 'group' },
      { label: 'Rewards', route: '/rewards', icon: 'workspace_premium' },
      { label: 'Analytics', route: '/analytics', icon: 'monitoring' }
    ];
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/welcome']);
  }
}
