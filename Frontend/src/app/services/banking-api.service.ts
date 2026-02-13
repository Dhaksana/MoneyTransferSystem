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
}