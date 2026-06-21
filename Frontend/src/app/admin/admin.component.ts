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
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminDashboard, AdminTransaction, AdminUser, AuditLog, UserDetail, UserAccountDetail, BankingApiService } from '../services/banking-api.service';

@Component({
    selector: 'app-admin',
    imports: [
        CommonModule, FormsModule, MatButtonModule, MatCardModule, MatIconModule,
        MatTableModule, MatPaginatorModule, MatFormFieldModule, MatInputModule,
        MatSelectModule, MatDatepickerModule, MatNativeDateModule, MatSnackBarModule, MatTooltipModule
    ],
    template: `
    <section class="admin-page">
      <div class="page-heading">
        <div><p class="eyebrow">Administrator</p><h1>Admin portal</h1></div>
      </div>

      @if (dashboard) {
        <div class="kpi-grid">
          <mat-card class="glass-card"><mat-card-content><span>Total users</span><strong>{{ dashboard.totalUsers }}</strong></mat-card-content></mat-card>
          <mat-card class="glass-card"><mat-card-content><span>Active users</span><strong>{{ dashboard.activeUsers }}</strong></mat-card-content></mat-card>
          <mat-card class="glass-card"><mat-card-content><span>Volume</span><strong>{{ dashboard.totalTransactionVolume | currency:'INR':'symbol':'1.0-0' }}</strong></mat-card-content></mat-card>
          <mat-card class="glass-card"><mat-card-content><span>Rewards</span><strong>{{ dashboard.totalRewardsDistributed }}</strong></mat-card-content></mat-card>
        </div>
      }

      <div class="section-tabs">
        <button mat-stroked-button [class.active]="tab === 'users'" (click)="switchTab('users')"><mat-icon>people</mat-icon>Users</button>
        <button mat-stroked-button [class.active]="tab === 'transactions'" (click)="switchTab('transactions')"><mat-icon>receipt_long</mat-icon>Transactions</button>
        <button mat-stroked-button [class.active]="tab === 'audit'" (click)="switchTab('audit')"><mat-icon>history</mat-icon>Audit Log</button>
      </div>

      @if (tab === 'users') {
        <div class="tab-layout">
          <div class="tab-primary">
            <mat-card class="glass-card">
              <mat-card-header><mat-card-title>User management</mat-card-title></mat-card-header>
              <mat-card-content>
                <table mat-table [dataSource]="users" class="full-table">
                  <ng-container matColumnDef="username"><th mat-header-cell *matHeaderCellDef>User</th><td mat-cell *matCellDef="let u" class="clickable" (click)="selectUser(u)">{{ u.username }}</td></ng-container>
                  <ng-container matColumnDef="fullName"><th mat-header-cell *matHeaderCellDef>Name</th><td mat-cell *matCellDef="let u" (click)="selectUser(u)">{{ u.fullName }}</td></ng-container>
                  <ng-container matColumnDef="email"><th mat-header-cell *matHeaderCellDef>Email</th><td mat-cell *matCellDef="let u" (click)="selectUser(u)">{{ u.email }}</td></ng-container>
                  <ng-container matColumnDef="role"><th mat-header-cell *matHeaderCellDef>Role</th><td mat-cell *matCellDef="let u"><span class="role-badge">{{ u.role }}</span></td></ng-container>
                  <ng-container matColumnDef="status"><th mat-header-cell *matHeaderCellDef>Status</th><td mat-cell *matCellDef="let u"><span class="status-pill" [class.active]="u.status === 'ACTIVE'" [class.blocked]="u.status !== 'ACTIVE'">{{ u.status }}</span></td></ng-container>
                  <ng-container matColumnDef="action">
                    <th mat-header-cell *matHeaderCellDef>Action</th>
                    <td mat-cell *matCellDef="let u">
                      <button mat-stroked-button (click)="startEdit(u)"><mat-icon>edit</mat-icon>Edit</button>
                      <button mat-stroked-button color="primary" (click)="toggleUser(u)">{{ u.status === 'ACTIVE' ? 'Block' : 'Unblock' }}</button>
                    </td>
                  </ng-container>
                  <tr mat-header-row *matHeaderRowDef="userColumns"></tr><tr mat-row *matRowDef="let row; columns: userColumns;" [class.selected-row]="selectedUser?.id === row.id"></tr>
                </table>
              </mat-card-content>
            </mat-card>
          </div>

          @if (selectedUserDetail) {
            <div class="tab-sidebar">
              <mat-card class="glass-card">
                <mat-card-header>
                  <mat-card-title>User details — {{ selectedUserDetail.username }}</mat-card-title>
                  <button mat-icon-button (click)="selectedUserDetail = null; selectedUser = null"><mat-icon>close</mat-icon></button>
                </mat-card-header>
                <mat-card-content>
                  <div class="detail-grid">
                    <div class="detail-item"><label>User ID</label><span>{{ selectedUserDetail.id }}</span></div>
                    <div class="detail-item"><label>Username</label><span>{{ selectedUserDetail.username }}</span></div>
                    <div class="detail-item"><label>Full name</label><span>{{ selectedUserDetail.fullName }}</span></div>
                    <div class="detail-item"><label>Display name</label><span>{{ selectedUserDetail.displayName || '-' }}</span></div>
                    <div class="detail-item"><label>Email</label><span>{{ selectedUserDetail.email }}</span></div>
                    <div class="detail-item"><label>Role</label><span class="role-badge">{{ selectedUserDetail.role }}</span></div>
                    <div class="detail-item"><label>Status</label><span class="status-pill" [class.active]="selectedUserDetail.status === 'ACTIVE'" [class.blocked]="selectedUserDetail.status !== 'ACTIVE'">{{ selectedUserDetail.status }}</span></div>
                    <div class="detail-item"><label>Account ID</label><span>{{ selectedUserDetail.accountId || '-' }}</span></div>
                  </div>
                  @if (selectedUserDetail.accounts.length > 0) {
                    <h3 class="sub-heading">Accounts ({{ selectedUserDetail.accounts.length }})</h3>
                    <div class="accounts-list">
                      @for (acc of selectedUserDetail.accounts; track acc.id) {
                        <div class="account-row">
                          <div class="account-info">
                            <strong>{{ acc.accountType }}</strong>
                            <span class="acct-num">{{ acc.accountNumber }}</span>
                            <span class="acct-holder">{{ acc.holderName }}</span>
                          </div>
                          <div class="account-meta">
                            <span class="acct-balance">{{ acc.balance | currency:'INR':'symbol':'1.2-2' }}</span>
                            <span class="status-pill" [class.active]="acc.status === 'ACTIVE'" [class.blocked]="acc.status !== 'ACTIVE'">{{ acc.status }}</span>
                            <button mat-icon-button (click)="editAccount(acc)" matTooltip="Edit account"><mat-icon>edit</mat-icon></button>
                          </div>
                        </div>
                      }
                    </div>
                  }
                  <div class="detail-actions">
                    <button mat-flat-button color="primary" (click)="startEdit(selectedUser)"><mat-icon>edit</mat-icon>Edit user</button>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          }
        </div>

        @if (editUser) {
          <div class="edit-overlay" (click)="cancelEdit()"></div>
          <mat-card class="edit-card">
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
                  <mat-label>Display name</mat-label>
                  <input matInput [(ngModel)]="editForm.displayName" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Email</mat-label>
                  <input matInput [(ngModel)]="editForm.email" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Account ID</mat-label>
                  <input matInput [(ngModel)]="editForm.accountId" />
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
        }

        @if (editAccountTarget) {
          <div class="edit-overlay" (click)="cancelEditAccount()"></div>
          <mat-card class="edit-card">
            <mat-card-header>
              <mat-card-title>Edit account — {{ editAccountTarget.accountNumber }}</mat-card-title>
              <button mat-icon-button (click)="cancelEditAccount()"><mat-icon>close</mat-icon></button>
            </mat-card-header>
            <mat-card-content>
              <div class="edit-grid">
                <mat-form-field appearance="outline">
                  <mat-label>Account type</mat-label>
                  <mat-select [(ngModel)]="editAccountForm.accountType">
                    <mat-option value="SAVINGS">SAVINGS</mat-option>
                    <mat-option value="CURRENT">CURRENT</mat-option>
                    <mat-option value="LOAN">LOAN</mat-option>
                  </mat-select>
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Holder name</mat-label>
                  <input matInput [(ngModel)]="editAccountForm.holderName" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Balance</mat-label>
                  <input matInput type="number" [(ngModel)]="editAccountForm.balance" />
                </mat-form-field>
                <mat-form-field appearance="outline">
                  <mat-label>Status</mat-label>
                  <mat-select [(ngModel)]="editAccountForm.status">
                    <mat-option value="ACTIVE">ACTIVE</mat-option>
                    <mat-option value="BLOCKED">BLOCKED</mat-option>
                    <mat-option value="INACTIVE">INACTIVE</mat-option>
                  </mat-select>
                </mat-form-field>
              </div>
              <div class="edit-actions">
                <button mat-flat-button color="primary" (click)="saveEditAccount()"><mat-icon>save</mat-icon>Save</button>
                <button mat-stroked-button (click)="cancelEditAccount()">Cancel</button>
              </div>
            </mat-card-content>
          </mat-card>
        }
      }

      @if (tab === 'transactions') {
        <div>
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
      }

      @if (tab === 'audit') {
        <div>
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
              @if (auditTotal === 0) {
                <div class="empty-state">No audit logs found.</div>
              }
              @if (auditTotal > 0) {
                <mat-paginator [length]="auditTotal" [pageSize]="auditPageSize" [pageIndex]="auditPage" (page)="onAuditPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
              }
            </mat-card-content>
          </mat-card>
        </div>
      }
    </section>
    `,
    styles: [`
    .admin-page { display:grid; gap:20px; }
    .page-heading { display:flex; justify-content:space-between; align-items:center; }
    .eyebrow { margin:0 0 6px; color:#0f766e; font-weight:800; text-transform:uppercase; letter-spacing:.08em; }
    h1 { margin:0; color:#0f2742; font:700 32px/1.15 Poppins, Inter, sans-serif; }
    .kpi-grid { display:grid; grid-template-columns:repeat(4,minmax(180px,1fr)); gap:14px; }
    .glass-card { border-radius:20px; background:rgba(255,255,255,.84); box-shadow:0 18px 50px rgba(15,39,66,.10); }
    .section-tabs { display:flex; gap:8px; flex-wrap:wrap; }
    .section-tabs button.active { background:var(--brand,#7c3aed); color:#fff; }

    .tab-layout { display:grid; grid-template-columns:1fr 380px; gap:20px; align-items:start; }
    .tab-primary { min-width:0; }
    .tab-sidebar { min-width:0; }

    .full-table { width:100%; }
    .full-table td.clickable { cursor:pointer; }
    .full-table td.clickable:hover { text-decoration:underline; }
    tr.selected-row { background:rgba(124,58,237,.06); }

    .status-pill { display:inline-block; padding:4px 10px; border-radius:999px; font-size:12px; font-weight:700; background:#f1f5f9; color:#64748b; white-space:nowrap; }
    .status-pill.active { background:#dcfce7; color:#166534; }
    .status-pill.blocked { background:#fee2e2; color:#991b1b; }
    .status-pill.failed { background:#fee2e2; color:#991b1b; }
    .role-badge { display:inline-block; padding:2px 8px; border-radius:6px; background:#ede9fe; color:#6d28d9; font-weight:700; font-size:12px; white-space:nowrap; }

    .detail-grid { display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-top:8px; }
    .detail-item { display:flex; flex-direction:column; gap:2px; }
    .detail-item label { font-size:11px; color:#64748b; text-transform:uppercase; letter-spacing:.05em; font-weight:700; }
    .detail-item span { font-size:14px; color:#0f2742; word-break:break-all; }
    .sub-heading { font:600 14px/1.4 Inter,sans-serif; color:#0f2742; margin:16px 0 8px; padding-top:12px; border-top:1px solid #e2e8f0; }
    .accounts-list { display:grid; gap:8px; }
    .account-row { display:flex; justify-content:space-between; align-items:center; padding:8px 10px; background:#f8fafc; border-radius:10px; }
    .account-info { display:flex; flex-direction:column; gap:1px; }
    .account-info strong { font-size:13px; color:#0f2742; }
    .account-info .acct-num { font-size:12px; color:#64748b; font-family:monospace; }
    .account-info .acct-holder { font-size:12px; color:#64748b; }
    .account-meta { display:flex; flex-direction:column; align-items:flex-end; gap:2px; }
    .acct-balance { font-weight:700; font-size:14px; color:#166534; }
    .detail-actions { margin-top:16px; padding-top:12px; border-top:1px solid #e2e8f0; }

    .audit-filters { display:grid; grid-template-columns:1fr 1fr 1fr 1fr auto; gap:12px; align-items:start; margin-bottom:16px; }
    .filter-actions { display:flex; gap:6px; padding-top:4px; flex-wrap:wrap; }
    .action-badge { padding:3px 10px; border-radius:6px; font-size:12px; font-weight:700; background:#f1f5f9; color:#334155; white-space:nowrap; }
    .action-badge.login { background:#dbeafe; color:#1e40af; }
    .action-badge.register { background:#ede9fe; color:#6d28d9; }
    .action-badge.transfer { background:#dcfce7; color:#166534; }
    .action-badge.beneficiary { background:#fef3c7; color:#92400e; }
    .action-badge.reward { background:#fce7f3; color:#9d174d; }
    .action-badge.admin { background:#fee2e2; color:#991b1b; }
    .details-cell { max-width:300px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .empty-state { padding:24px; text-align:center; color:#64748b; }

    .edit-overlay { position:fixed; inset:0; background:rgba(0,0,0,.3); z-index:100; }
    .edit-card { position:fixed; top:50%; left:50%; transform:translate(-50%,-50%); width:min(480px,94vw); z-index:101; border-radius:16px; max-height:90vh; overflow-y:auto; }
    .edit-card mat-card-header { display:flex; justify-content:space-between; align-items:center; }
    .edit-grid { display:grid; grid-template-columns:1fr 1fr; gap:14px; padding-top:12px; }
    .edit-actions { display:flex; gap:10px; justify-content:flex-end; margin-top:12px; }

    @media (max-width:1100px) { .tab-layout { grid-template-columns:1fr; } .tab-sidebar { order:-1; } }
    @media (max-width:900px) { .audit-filters { grid-template-columns:1fr 1fr; } .kpi-grid { grid-template-columns:1fr 1fr; } .edit-grid { grid-template-columns:1fr; } }
    @media (max-width:600px) { .kpi-grid { grid-template-columns:1fr; } .audit-filters { grid-template-columns:1fr; } }
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
  selectedUser: AdminUser | null = null;
  selectedUserDetail: UserDetail | null = null;
  editForm: { fullName: string; displayName: string; email: string; role: string; status: string; accountId: string } = { fullName: '', displayName: '', email: '', role: 'USER', status: 'ACTIVE', accountId: '' };
  editAccountTarget: UserAccountDetail | null = null;
  editAccountForm: { accountType: string; holderName: string; balance: number; status: string } = { accountType: 'SAVINGS', holderName: '', balance: 0, status: 'ACTIVE' };

  constructor(private api: BankingApiService, private snack: MatSnackBar) {}

  ngOnInit() { this.load(); }

  load() {
    this.api.getAdminDashboard().subscribe(d => this.dashboard = d);
    this.api.getAdminUsers().subscribe(u => {
      this.users = u;
      if (u.length > 0 && !this.selectedUser) {
        this.selectUser(u[0]);
      }
    });
    this.api.getAdminTransactions().subscribe(t => this.transactions = t);
    this.searchAudit();
  }

  switchTab(t: 'users' | 'transactions' | 'audit') {
    this.tab = t;
    if (t === 'users' && this.users.length > 0 && !this.selectedUser) {
      this.selectUser(this.users[0]);
    }
  }

  selectUser(u: AdminUser) {
    this.selectedUser = u;
    this.api.getAdminUserDetails(u.id).subscribe(d => this.selectedUserDetail = d);
  }

  toggleUser(user: AdminUser) {
    this.api.setAdminUserStatus(user.id, user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE').subscribe(() => this.load());
  }

  startEdit(u: AdminUser | null) {
    if (!u) return;
    this.editUser = u;
    this.editForm = { fullName: u.fullName, displayName: u.displayName || '', email: u.email, role: u.role, status: u.status, accountId: u.accountId || '' };
  }

  cancelEdit() {
    this.editUser = null;
  }

  editAccount(acc: UserAccountDetail) {
    this.editAccountTarget = acc;
    this.editAccountForm = { accountType: acc.accountType, holderName: acc.holderName, balance: acc.balance, status: acc.status };
  }

  cancelEditAccount() {
    this.editAccountTarget = null;
  }

  saveEditAccount() {
    if (!this.editAccountTarget) return;
    const target = this.editAccountTarget;
    this.editAccountTarget = null;
    this.api.updateAdminAccount(target.id, this.editAccountForm).subscribe({
      next: () => {
        this.snack.open('Account updated', 'Close', { duration: 2400 });
        if (this.selectedUser) this.selectUser(this.selectedUser);
      },
      error: () => this.snack.open('Failed to update account', 'Close', { duration: 2400 })
    });
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
