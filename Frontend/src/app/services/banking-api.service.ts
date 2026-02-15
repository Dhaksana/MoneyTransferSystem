import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, map, throwError } from 'rxjs';

export type TxStatus = 'ACTIVE' | 'INACTIVE' | 'SUCCESS' | 'FAILED' | 'PENDING' | string;

export interface TransferHistoryItem {
  transactionId: number;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  status: TxStatus;
  failureReason: string | null;
  createdOn: string; // ISO date-time
}

export interface TransferResponseDTO {
  transactionId: number;
  status: string;
  message: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
  isFirst: boolean;
}

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

  /** GET /accounts/{id} -> Full AccountDTO with holder name and balance */
  getAccountById(accountId: string) {
    return this.http.get<any>(`${this.baseUrl}/accounts/${encodeURIComponent(accountId)}`)
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error?.error || err.message || 'Failed to load account details';
          return throwError(() => new Error(msg));
        })
      );
  }

  /** GET /transfers/history/{accountId} -> TransferHistoryItem[] */
  getHistoryByAccount(accountId: string) {
    return this.http.get<TransferHistoryItem[]>(`${this.baseUrl}/transfers/history/${encodeURIComponent(accountId)}`)
      .pipe(
        map((res: any) => Array.isArray(res) ? res : (res?.items || [])),
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

  /** POST /transfers -> TransferResponseDTO (includes idempotencyKey in body and header) */
  transfer(fromAccountId: string, toAccountId: string, amount: number, providedKey?: string) {
    const idempotencyKey =
      (providedKey?.trim()) ||
      (typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() :
       'ik-' + Math.random().toString(36).slice(2) + Date.now().toString(36));

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey
    });

    const body = { fromAccountId, toAccountId, amount, idempotencyKey };

    return this.http.post<TransferResponseDTO>(`${this.baseUrl}/transfers`, body, { headers })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error?.error || err.message || 'Transfer failed';
          return throwError(() => new Error(msg));
        })
      );
  }

  /** GET /transfers/history/{accountId}/paginated?page=0&size=10 */
  getHistoryByAccountPaginated(accountId: string, page: number = 0, size: number = 10) {
    return this.http.get<PaginatedResponse<TransferHistoryItem>>(
      `${this.baseUrl}/transfers/history/${encodeURIComponent(accountId)}/paginated?page=${page}&size=${size}`
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        const msg = err.error?.message || err.error?.error || err.message || 'Failed to load history';
        return throwError(() => new Error(msg));
      })
    );
  }

  /** GET /admin/transactions/paginated?page=0&size=10 - Admin only */
  getAllTransactionsPaginated(page: number = 0, size: number = 10) {
    return this.http.get<PaginatedResponse<TransferHistoryItem>>(
      `${this.baseUrl}/admin/transactions/paginated?page=${page}&size=${size}`
    ).pipe(
      catchError((err: HttpErrorResponse) => {
        const msg = err.error?.message || err.error?.error || err.message || 'Failed to load transactions';
        return throwError(() => new Error(msg));
      })
    );
  }

  /** GET /admin/accounts - Admin only */
  getAllAccounts() {
    return this.http.get<any[]>(`${this.baseUrl}/admin/accounts`).pipe(
      catchError((err: HttpErrorResponse) => {
        const msg = err.error?.message || err.error?.error || err.message || 'Failed to load accounts';
        return throwError(() => new Error(msg));
      })
    );
  }

  /** PUT /admin/accounts/{id} - Admin only */
  updateAccount(accountId: string, body: any) {
    return this.http.put<any>(`${this.baseUrl}/admin/accounts/${encodeURIComponent(accountId)}`, body).pipe(
      catchError((err: HttpErrorResponse) => {
        const msg = err.error?.message || err.error?.error || err.message || 'Failed to update account';
        return throwError(() => new Error(msg));
      })
    );
  }

  /** DELETE /admin/accounts/{id} - Admin only */
  deactivateAccount(accountId: string) {
    return this.http.delete<void>(`${this.baseUrl}/admin/accounts/${encodeURIComponent(accountId)}`).pipe(
      catchError((err: HttpErrorResponse) => {
        const msg = err.error?.message || err.error?.error || err.message || 'Failed to deactivate account';
        return throwError(() => new Error(msg));
      })
    );
  }
}