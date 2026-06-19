import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AdminDashboard, AdminTransaction, AdminUser, AuditLog, BankingApiService } from '../services/banking-api.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatButtonModule, MatCardModule, MatIconModule,
    MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatSnackBarModule
  ],
  template: `
    <section class="admin-page">
      <div class="page-heading">
        <div><p class="eyebrow">Administrator</p><h1>Admin portal</h1></div>
      </div>

      <div class="kpi-grid" *ngIf="dashboard">
        <mat-card class="glass-card"><mat-card-content><span>Total users</span><strong>{{ dashboard.totalUsers }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Active users</span><strong>{{ dashboard.activeUsers }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Volume</span><strong>{{ dashboard.totalTransactionVolume | currency:'INR':'symbol':'1.0-0' }}</strong></mat-card-content></mat-card>
        <mat-card class="glass-card"><mat-card-content><span>Rewards</span><strong>{{ dashboard.totalRewardsDistributed }}</strong></mat-card-content></mat-card>
      </div>

      <div class="section-tabs">
        <button mat-stroked-button [class.active]="tab === 'users'" (click)="tab = 'users'"><mat-icon>people</mat-icon>Users</button>
        <button mat-stroked-button [class.active]="tab === 'transactions'" (click)="tab = 'transactions'"><mat-icon>receipt_long</mat-icon>Transactions</button>
        <button mat-stroked-button [class.active]="tab === 'audit'" (click)="tab = 'audit'"><mat-icon>history</mat-icon>Audit Log</button>
      </div>

      <div *ngIf="tab === 'users'">
        <mat-card class="glass-card">
          <mat-card-header><mat-card-title>User management</mat-card-title></mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="users" class="full-table">
              <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>User</th><td mat-cell *matCellDef="let u">{{ u.username }}</td></ng-container>
              <ng-container matColumnDef="fullName"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let u">{{ u.fullName }}</td></ng-container>
              <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>Email</th><td mat-cell *matCellDef="let u">{{ u.email }}</td></ng-container>
              <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Role</th><td mat-cell *matCellDef="let u"><span class="role-badge">{{ u.role }}</span></td></ng-container>
              <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let u"><span class="status-pill" [class.active]="u.status === 'ACTIVE'" [class.blocked]="u.status !== 'ACTIVE'">{{ u.status }}</span></td></ng-container>
              <ng-container matColumnDef="action">
                <th mat-header-cell *matHeaderCellDef>Action</th>
                <td mat-cell *matCellDef="let u">
                  <button mat-stroked-button (click)="startEdit(u)"><mat-icon>edit</mat-icon>Edit</button>
                  <button mat-stroked-button color="primary" (click)="toggleUser(u)">{{ u.status === 'ACTIVE' ? 'Block' : 'Unblock' }}</button>
                </td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="userColumns"></tr><tr mat-row *matRowDef="let row; columns: userColumns;"></tr>
            </table>
          </mat-card-content>
        </mat-card>

        <div class="edit-overlay" *ngIf="editUser" (click)="cancelEdit()"></div>
        <mat-card class="edit-card" *ngIf="editUser">
          <mat-card-header>
            <mat-card-title>Edit user — {{ editUser.username }}</mat-card-title>
            <button mat-icon-button (click)="cancelEdit()"><mat-icon>close</mat-icon></button>
          </mat-card-header>
          <mat-card-content>
            <div class="edit-grid">
              <mat-form-field appearance="outline">
                <mat-label>Full name</mat-label>
                <input matInput [(ngModel)]="editForm.fullName" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Email</mat-label>
                <input matInput [(ngModel)]="editForm.email" />
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Role</mat-label>
                <mat-select [(ngModel)]="editForm.role">
                  <mat-option value="USER">USER</mat-option>
                  <mat-option value="ADMIN">ADMIN</mat-option>
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Status</mat-label>
                <mat-select [(ngModel)]="editForm.status">
                  <mat-option value="ACTIVE">ACTIVE</mat-option>
                  <mat-option value="BLOCKED">BLOCKED</mat-option>
                  <mat-option value="INACTIVE">INACTIVE</mat-option>
                </mat-select>
              </mat-form-field>
            </div>
            <div class="edit-actions">
              <button mat-flat-button color="primary" (click)="saveEdit()"><mat-icon>save</mat-icon>Save</button>
              <button mat-stroked-button (click)="cancelEdit()">Cancel</button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <div *ngIf="tab === 'transactions'">
        <mat-card class="glass-card">
          <mat-card-header><mat-card-title>Transaction monitoring</mat-card-title></mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="transactions" class="full-table">
              <ng-container matColumnDef="referenceNumber"><th mat-header-cell *matHeaderCellDef>Reference</th><td mat-cell *matCellDef="let t">{{ t.referenceNumber }}</td></ng-container>
              <ng-container matColumnDef="fromAccount"><th mat-header-cell *matHeaderCellDef>From</th><td mat-cell *matCellDef="let t">{{ t.fromAccount }}</td></ng-container>
              <ng-container matColumnDef="toAccount"><th mat-header-cell *matHeaderCellDef>To</th><td mat-cell *matCellDef="let t">{{ t.toAccount }}</td></ng-container>
              <ng-container matColumnDef="amount"><th mat-header-cell *matHeaderCellDef>Amount</th><td mat-cell *matCellDef="let t">{{ t.amount | currency:'INR':'symbol':'1.0-0' }}</td></ng-container>
              <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let t"><span class="status-pill" [class.active]="t.status === 'SUCCESS'" [class.failed]="t.status === 'FAILED'">{{ t.status }}</span></td></ng-container>
              <ng-container matColumnDef="createdAt"><th mat-header-cell *matHeaderCellDef>Date</th><td mat-cell *matCellDef="let t">{{ t.createdAt | date:'medium' }}</td></ng-container>
              <tr mat-header-row *matHeaderRowDef="transactionColumns"></tr><tr mat-row *matRowDef="let row; columns: transactionColumns;"></tr>
            </table>
          </mat-card-content>
        </mat-card>
      </div>

      <div *ngIf="tab === 'audit'">
        <mat-card class="glass-card">
          <mat-card-header><mat-card-title>Audit trail</mat-card-title></mat-card-header>
          <mat-card-content>
            <div class="audit-filters">
              <mat-form-field appearance="outline">
                <mat-label>Search action</mat-label>
                <input matInput [(ngModel)]="auditFilter.action" placeholder="e.g. LOGIN, TRANSFER" />
                <mat-icon matSuffix>search</mat-icon>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Username</mat-label>
                <input matInput [(ngModel)]="auditFilter.username" placeholder="Filter by user" />
                <mat-icon matSuffix>person</mat-icon>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>From date</mat-label>
                <input matInput [matDatepicker]="fromPicker" [(ngModel)]="auditFilter.fromDate" />
                <mat-datepicker-toggle matSuffix [for]="fromPicker"></mat-datepicker-toggle>
                <mat-datepicker #fromPicker></mat-datepicker>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>To date</mat-label>
                <input matInput [matDatepicker]="toPicker" [(ngModel)]="auditFilter.toDate" />
                <mat-datepicker-toggle matSuffix [for]="toPicker"></mat-datepicker-toggle>
                <mat-datepicker #toPicker></mat-datepicker>
              </mat-form-field>
              <div class="filter-actions">
                <button mat-flat-button color="primary" (click)="searchAudit()"><mat-icon>filter_alt</mat-icon>Search</button>
                <button mat-stroked-button (click)="resetAuditFilter()"><mat-icon>clear</mat-icon>Reset</button>
                <button mat-stroked-button (click)="exportAudit()"><mat-icon>download</mat-icon>Export CSV</button>
              </div>
            </div>

            <table mat-table [dataSource]="auditLogs" class="full-table">
              <ng-container matColumnDef="id"><th mat-header-cell *matHeaderCellDef>#</th><td mat-cell *matCellDef="let log">{{ log.id }}</td></ng-container>
              <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>User</th><td mat-cell *matCellDef="let log">{{ log.username || '-' }}</td></ng-container>
              <ng-container matColumnDef="action"><th mat-header-cell *matHeaderCellDef>Action</th><td mat-cell *matCellDef="let log"><span class="action-badge" [class]="actionClass(log.action)">{{ log.action }}</span></td></ng-container>
              <ng-container matColumnDef="details"><th mat-header-cell *matHeaderCellDef>Details</th><td mat-cell *matCellDef="let log" class="details-cell">{{ log.details }}</td></ng-container>
              <ng-container matColumnDef="ipAddress"><th mat-header-cell *matHeaderCellDef>IP</th><td mat-cell *matCellDef="let log">{{ log.ipAddress || '-' }}</td></ng-container>
              <ng-container matColumnDef="timestamp"><th mat-header-cell *matHeaderCellDef>Timestamp</th><td mat-cell *matCellDef="let log">{{ log.timestamp | date:'medium' }}</td></ng-container>
              <ng-container matColumnDef="actions"><th mat-header-cell *matHeaderCellDef></th><td mat-cell *matCellDef="let log"><button mat-icon-button color="warn" (click)="deleteAuditEntry(log)"><mat-icon>delete</mat-icon></button></td></ng-container>
              <tr mat-header-row *matHeaderRowDef="auditColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: auditColumns;"></tr>
            </table>
            <div *ngIf="auditTotal === 0" class="empty-state">No audit logs found.</div>
            <mat-paginator *ngIf="auditTotal > 0" [length]="auditTotal" [pageSize]="auditPageSize" [pageIndex]="auditPage" (page)="onAuditPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
          </mat-card-content>
        </mat-card>
      </div>
    </section>
  `,
  styles: [`
    .admin-page { display:grid; gap:20px; }
    .page-heading { display:flex; justify-content:space-between; align-items:center; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .kpi-grid { display:grid; grid-template-columns:repeat(4,minmax(180px,1fr)); gap:14px; }
    .glass-card { border-radius:20px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); }
    .section-tabs { display:flex; gap:8px; }
    .section-tabs button.active { background:var(--brand,#7c3aed); color:#fff; }
    .full-table { width:100%; }
    .status-pill { padding:4px 10px; border-radius:999px; font-size:12px; font-weight:700; background:#f1f5f9; color:#64748b; }
    .status-pill.active { background:#dcfce7; color:#166534; }
    .status-pill.blocked { background:#fee2e2; color:#991b1b; }
    .status-pill.failed { background:#fee2e2; color:#991b1b; }
    .role-badge { padding:2px 8px; border-radius:6px; background:#ede9fe; color:#6d28d9; font-weight:700; font-size:12px; }
    .audit-filters { display:grid; grid-template-columns:1fr 1fr 1fr 1fr auto; gap:12px; align-items:start; margin-bottom:16px; }
    .filter-actions { display:flex; gap:6px; padding-top:4px; }
    .action-badge { padding:3px 10px; border-radius:6px; font-size:12px; font-weight:700; background:#f1f5f9; color:#334155; }
    .action-badge.login { background:#dbeafe; color:#1e40af; }
    .action-badge.register { background:#ede9fe; color:#6d28d9; }
    .action-badge.transfer { background:#dcfce7; color:#166534; }
    .action-badge.beneficiary { background:#fef3c7; color:#92400e; }
    .action-badge.reward { background:#fce7f3; color:#9d174d; }
    .action-badge.admin { background:#fee2e2; color:#991b1b; }
    .details-cell { max-width:300px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .empty-state { padding:24px; text-align:center; color:#64748b; }
    .edit-overlay { position:fixed; inset:0; background:rgba(0,0,0,.3); z-index:100; }
    .edit-card { position:fixed; top:50%; left:50%; transform:translate(-50%,-50%); width:min(480px,94vw); z-index:101; border-radius:16px; }
    .edit-card mat-card-header { display:flex; justify-content:space-between; align-items:center; }
    .edit-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; padding-top:12px; }
    .edit-actions { display:flex; gap:10px; justify-content:flex-end; margin-top:12px; }
    @media (max-width:900px) { .audit-filters { grid-template-columns:1fr 1fr; } .kpi-grid { grid-template-columns:1fr 1fr; } .edit-grid { grid-template-columns:1fr; } }
  `]
})
export class AdminComponent implements OnInit {
  tab: 'users' | 'transactions' | 'audit' = 'users';
  dashboard?: AdminDashboard;
  users: AdminUser[] = [];
  transactions: AdminTransaction[] = [];
  userColumns = ['username', 'fullName', 'email', 'role', 'status', 'action'];
  transactionColumns = ['referenceNumber', 'fromAccount', 'toAccount', 'amount', 'status', 'createdAt'];
  auditColumns = ['id', 'username', 'action', 'details', 'ipAddress', 'timestamp', 'actions'];
  auditLogs: AuditLog[] = [];
  auditTotal = 0;
  auditPage = 0;
  auditPageSize = 20;
  auditFilter: { action: string; username: string; fromDate: Date | null; toDate: Date | null } = { action: '', username: '', fromDate: null, toDate: null };
  editUser: AdminUser | null = null;
  editForm: { fullName: string; email: string; role: string; status: string } = { fullName: '', email: '', role: 'USER', status: 'ACTIVE' };

  constructor(private api: BankingApiService, private snack: MatSnackBar) {}

  ngOnInit() { this.load(); }

  load() {
    this.api.getAdminDashboard().subscribe(d => this.dashboard = d);
    this.api.getAdminUsers().subscribe(u => this.users = u);
    this.api.getAdminTransactions().subscribe(t => this.transactions = t);
    this.searchAudit();
  }

  toggleUser(user: AdminUser) {
    this.api.setAdminUserStatus(user.id, user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE').subscribe(() => this.load());
  }

  startEdit(u: AdminUser) {
    this.editUser = u;
    this.editForm = { fullName: u.fullName, email: u.email, role: u.role, status: u.status };
  }

  cancelEdit() {
    this.editUser = null;
  }

  saveEdit() {
    if (!this.editUser) return;
    this.api.updateAdminUser(this.editUser.id, this.editForm).subscribe({
      next: () => {
        this.snack.open('User updated', 'Close', { duration: 2400 });
        this.editUser = null;
        this.load();
      },
      error: () => this.snack.open('Failed to update user', 'Close', { duration: 2400 })
    });
  }

  searchAudit() {
    const params: any = { page: this.auditPage, size: this.auditPageSize };
    if (this.auditFilter.action) params.action = this.auditFilter.action;
    if (this.auditFilter.username) params.username = this.auditFilter.username;
    if (this.auditFilter.fromDate) params.from = this.auditFilter.fromDate.toISOString();
    if (this.auditFilter.toDate) params.to = this.auditFilter.toDate.toISOString();
    this.api.searchAuditLogs(params).subscribe(r => {
      this.auditLogs = r.content;
      this.auditTotal = r.totalElements;
    });
  }

  resetAuditFilter() {
    this.auditFilter = { action: '', username: '', fromDate: null, toDate: null };
    this.auditPage = 0;
    this.searchAudit();
  }

  onAuditPage(e: PageEvent) {
    this.auditPage = e.pageIndex;
    this.auditPageSize = e.pageSize;
    this.searchAudit();
  }

  exportAudit() {
    const params: any = {};
    if (this.auditFilter.action) params.action = this.auditFilter.action;
    if (this.auditFilter.username) params.username = this.auditFilter.username;
    if (this.auditFilter.fromDate) params.from = this.auditFilter.fromDate.toISOString();
    if (this.auditFilter.toDate) params.to = this.auditFilter.toDate.toISOString();
    this.api.exportAuditLogs(params).subscribe(logs => {
      const csv = ['ID,User,Action,Details,IP,Timestamp',
        ...logs.map(l => `"${l.id}","${l.username || ''}","${l.action}","${(l.details || '').replace(/"/g, '""')}","${l.ipAddress || ''}","${l.timestamp}"`)
      ].join('\n');
      this.api.saveBlob(new Blob([csv], { type: 'text/csv' }), `audit-logs-${new Date().toISOString().slice(0,10)}.csv`);
      this.snack.open('Audit logs exported', 'Close', { duration: 2400 });
    });
  }

  deleteAuditEntry(log: AuditLog) {
    this.api.deleteAuditLog(log.id).subscribe(() => {
      this.searchAudit();
      this.snack.open('Audit entry deleted', 'Close', { duration: 2400 });
    });
  }

  actionClass(action: string): string {
    const a = action.toLowerCase();
    if (a.includes('login')) return 'login';
    if (a.includes('register')) return 'register';
    if (a.includes('transfer')) return 'transfer';
    if (a.includes('beneficiary')) return 'beneficiary';
    if (a.includes('reward')) return 'reward';
    if (a.includes('admin') || a.includes('block') || a.includes('unblock')) return 'admin';
    return '';
  }
}
