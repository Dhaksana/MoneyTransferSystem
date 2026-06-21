import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { catchError, map, throwError } from 'rxjs';
import { TransferHistoryItem, TransferResponseDTO, normalizeHistoryItems } from '../models/transfer.model';

export type { TransferHistoryItem, TransferResponseDTO } from '../models/transfer.model';

export interface Beneficiary {
  id?: number;
  ownerAccountId: string;
  beneficiaryName: string;
  beneficiaryAccountNumber: string;
  bankName?: string;
  ifsc?: string;
  nickname?: string;
  favorite: boolean;
}

export interface RewardSummary { currentPoints: number; lifetimePoints: number; }
export interface RewardHistory { id: number; transactionId: number; pointsEarned: number; reason: string; createdAt: string; }
export interface ChartPoint { label: string; value: number; }
export interface AnalyticsSummary {
  moneySentThisMonth: number;
  moneyReceivedThisMonth: number;
  transactionCount: number;
  largestTransaction: number;
  mostFrequentBeneficiary: string;
  rewardPointsEarnedMonthly: number;
  monthlyTransactionTrend: ChartPoint[];
  statusDistribution: ChartPoint[];
}
export interface AppNotification { id: number; message: string; type: string; read: boolean; createdAt: string; }
export interface AdminUser { id: number; username: string; fullName: string; email: string; role: string; status: string; accountId: string; displayName?: string; }
export interface UserAccountDetail { id: string; accountNumber: string; accountType: string; holderName: string; balance: number; status: string; }
export interface UserDetail { id: number; username: string; fullName: string; email: string; role: string; status: string; accountId: string; displayName?: string; accounts: UserAccountDetail[]; }
export interface AdminTransaction { id: number; referenceNumber: string; fromAccount: string; toAccount: string; amount: number; status: string; createdAt: string; }
export interface AuditLog { id: number; username: string; action: string; details: string; ipAddress: string; timestamp: string; }
export interface AdminDashboard { totalUsers: number; activeUsers: number; totalTransactionVolume: number; successfulTransactions: number; failedTransactions: number; totalRewardsDistributed: number; }

@Injectable({ providedIn: 'root' })
export class BankingApiService {
  constructor(
    private http: HttpClient,
    @Inject('API_BASE_URL') private baseUrl: string // e.g. http://localhost:8080/api/v1
  ) {}

  /** GET /accounts/{id} -> Account-like JSON with balance */
  getBalance(accountId: string) {
    return this.http.get<any>(`${this.baseUrl}/accounts/${encodeURIComponent(accountId)}`).pipe(
      map(acc => {
        const val = acc?.balance;
        const num = typeof val === 'string' ? Number(val) : val; // handle "123.45" strings too
        return Number.isFinite(num) ? num : null;
      }),
      catchError(() => throwError(() => new Error('Failed to load balance')))
    );
  }

  /** GET /transfers/history/{accountId} -> TransferHistoryItem[] */
  getHistoryByAccount(accountId: string) {
    return this.http.get<TransferHistoryItem[]>(`${this.baseUrl}/transfers/history/${encodeURIComponent(accountId)}`)
      .pipe(
        map((res: any) => normalizeHistoryItems(res)),
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error?.error || err.message || 'Failed to load history';
          return throwError(() => new Error(msg));
        })
      );
  }

  /** GET /accounts/exists/{id} -> boolean */
  accountExists(accountId: string) {
    return this.http.get<boolean>(`${this.baseUrl}/accounts/exists/${encodeURIComponent(accountId)}`)
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.message || 'Failed to check account existence';
          return throwError(() => new Error(msg));
        })
      );
  }

  accountNumberExists(accountNumber: string) {
    return this.http.get<boolean>(`${this.baseUrl}/accounts/exists/account-number/${encodeURIComponent(accountNumber)}`)
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.message || 'Failed to check account number';
          return throwError(() => new Error(msg));
        })
      );
  }

  /** POST /transfers -> TransferResponseDTO (includes idempotencyKey in body and header) */
  transfer(fromAccountId: string, toAccountId: string, amount: number, providedKey?: string, useAccountNumbers = false) {
    const idempotencyKey =
      (providedKey?.trim()) ||
      (typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() :
       'ik-' + Math.random().toString(36).slice(2) + Date.now().toString(36));

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey
    });

    const body = useAccountNumbers
      ? { fromAccountNumber: fromAccountId, toAccountNumber: toAccountId, amount, idempotencyKey }
      : { fromAccountId, toAccountId, amount, idempotencyKey };

    return this.http.post<TransferResponseDTO>(`${this.baseUrl}/transfers`, body, { headers })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error?.error || err.message || 'Transfer failed';
          return throwError(() => new Error(msg));
        })
      );
  }

  getBeneficiaries() {
    return this.http.get<Beneficiary[]>(`${this.baseUrl}/beneficiaries`);
  }

  saveBeneficiary(beneficiary: Beneficiary) {
    return beneficiary.id
      ? this.http.put<Beneficiary>(`${this.baseUrl}/beneficiaries/${beneficiary.id}`, beneficiary)
      : this.http.post<Beneficiary>(`${this.baseUrl}/beneficiaries`, beneficiary);
  }

  deleteBeneficiary(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/beneficiaries/${id}`);
  }

  getRewardSummary() {
    return this.http.get<RewardSummary>(`${this.baseUrl}/rewards/summary`);
  }

  getRewardHistory() {
    return this.http.get<RewardHistory[]>(`${this.baseUrl}/rewards/history`);
  }

  getAnalytics() {
    return this.http.get<AnalyticsSummary>(`${this.baseUrl}/analytics/me`);
  }

  downloadReceipt(transactionId: number) {
    return this.http.get(`${this.baseUrl}/documents/receipts/${transactionId}`, { responseType: 'blob' });
  }

  downloadStatement(accountId: string, from: string, to: string) {
    return this.http.get(`${this.baseUrl}/documents/statements/${encodeURIComponent(accountId)}?from=${from}&to=${to}`, { responseType: 'blob' });
  }

  getNotifications() {
    return this.http.get<AppNotification[]>(`${this.baseUrl}/notifications`);
  }

  getUnreadCount() {
    return this.http.get<{ count: number }>(`${this.baseUrl}/notifications/unread-count`);
  }

  markNotificationRead(id: number) {
    return this.http.patch<AppNotification>(`${this.baseUrl}/notifications/${id}/read`, {});
  }

  deleteNotification(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/notifications/${id}`);
  }

  getAdminDashboard() {
    return this.http.get<AdminDashboard>(`${this.baseUrl}/admin/dashboard`);
  }

  getAdminUsers() {
    return this.http.get<AdminUser[]>(`${this.baseUrl}/admin/users`);
  }

  setAdminUserStatus(id: number, status: string) {
    return this.http.patch<AdminUser>(`${this.baseUrl}/admin/users/${id}/status`, { status });
  }

  getAdminUserDetails(id: number) {
    return this.http.get<UserDetail>(`${this.baseUrl}/admin/users/${id}`);
  }

  updateAdminUser(id: number, data: { fullName?: string; email?: string; role?: string; status?: string; displayName?: string; accountId?: string }) {
    return this.http.put<AdminUser>(`${this.baseUrl}/admin/users/${id}`, data);
  }

  updateAdminAccount(id: string, data: { accountType?: string; holderName?: string; balance?: number; status?: string }) {
    return this.http.put<UserAccountDetail>(`${this.baseUrl}/admin/accounts/${id}`, data);
  }

  getAdminTransactions() {
    return this.http.get<AdminTransaction[]>(`${this.baseUrl}/admin/transactions`);
  }

  getAdminAuditLogs() {
    return this.http.get<AuditLog[]>(`${this.baseUrl}/admin/audit-logs`);
  }

  searchAuditLogs(params: { action?: string; username?: string; from?: string; to?: string; page?: number; size?: number }) {
    let p = new HttpParams();
    if (params.action) p = p.set('action', params.action);
    if (params.username) p = p.set('username', params.username);
    if (params.from) p = p.set('from', params.from);
    if (params.to) p = p.set('to', params.to);
    p = p.set('page', params.page ?? 0);
    p = p.set('size', params.size ?? 20);
    return this.http.get<{ content: AuditLog[]; totalElements: number; totalPages: number; number: number }>(`${this.baseUrl}/admin/audit-logs/search`, { params: p });
  }

  exportAuditLogs(params: { action?: string; username?: string; from?: string; to?: string }) {
    let p = new HttpParams();
    if (params.action) p = p.set('action', params.action);
    if (params.username) p = p.set('username', params.username);
    if (params.from) p = p.set('from', params.from);
    if (params.to) p = p.set('to', params.to);
    return this.http.get<AuditLog[]>(`${this.baseUrl}/admin/audit-logs/export`, { params: p });
  }

  deleteAuditLog(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/admin/audit-logs/${id}`);
  }

  saveBlob(blob: Blob, filename: string) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
