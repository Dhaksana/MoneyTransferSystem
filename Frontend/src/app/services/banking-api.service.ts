import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, map, throwError } from 'rxjs';

export type TxStatus = 'ACTIVE' | 'INACTIVE' | 'SUCCESS' | 'FAILED' | 'PENDING' | string;

export interface TransferHistoryItem {
  transactionId: number;
  fromAccountId: number;
  toAccountId: number;
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

  /** GET /accounts/{id}/balance -> plain text number */
  // src/app/services/banking-api.service.ts
  getBalance(accountId: number) {
    // Expect an Account-like JSON: { id, holderName, balance, ... }
    return this.http.get<any>(`${this.baseUrl}/accounts/${accountId}`).pipe(
      map(acc => {
        const val = acc?.balance;
        const num = typeof val === 'string' ? Number(val) : val; // handle "123.45" strings too
        return Number.isFinite(num) ? num : null;
      }),
      catchError(() => throwError(() => new Error('Failed to load balance')))
    );
  }

  /** GET /transfers/history/{accountId} -> TransferHistoryItem[] */
  getHistoryByAccount(accountId: number) {
    return this.http.get<TransferHistoryItem[]>(`${this.baseUrl}/transfers/history/${accountId}`)
      .pipe(
        map((res: any) => Array.isArray(res) ? res : (res?.items || [])),
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.error?.error || err.message || 'Failed to load history';
          return throwError(() => new Error(msg));
        })
      );
  }

  /** POST /transfers -> TransferResponseDTO (includes idempotencyKey in body and header) */
  transfer(fromAccountId: number, toAccountId: number, amount: number, providedKey?: string) {
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