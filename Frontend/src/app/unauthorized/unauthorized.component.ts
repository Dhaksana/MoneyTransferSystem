import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-unauthorized',
    imports: [RouterLink, MatButtonModule, MatCardModule, MatIconModule],
    template: `
    <section class="auth-state">
      <mat-card class="glass-card state-card">
        <mat-icon>lock</mat-icon>
        <h2>Access restricted</h2>
        <p>Your session does not have permission to open this area.</p>
        <a mat-flat-button color="primary" routerLink="/profile">Back to dashboard</a>
      </mat-card>
    </section>
  `,
    changeDetection: ChangeDetectionStrategy.Eager,
    styles: [`
    .auth-state { min-height: 70vh; display: grid; place-items: center; }
    .state-card { width: min(420px, 92vw); text-align: center; padding: 32px; }
    mat-icon { width: 56px; height: 56px; font-size: 56px; color: #0f766e; margin: 0 auto 12px; }
    h2 { margin: 0 0 8px; color: #0f2742; }
    p { color: #64748b; }
  `]
})
export class UnauthorizedComponent {}
