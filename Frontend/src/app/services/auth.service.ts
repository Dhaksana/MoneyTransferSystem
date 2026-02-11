// src/app/auth.service.ts
import { Injectable, Inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, catchError, map, throwError } from 'rxjs';

interface LoginResponse {
  authenticated?: boolean;
  token?: string;
  user?: { id?: number; name?: string };
}

export interface AppUser {
  id: number | null;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _isLoggedIn = new BehaviorSubject<boolean>(this.hasSession());
  isLoggedIn$ = this._isLoggedIn.asObservable();

  private _currentUser = new BehaviorSubject<AppUser>(this.readUserFromStorage());
  currentUser$ = this._currentUser.asObservable();

  constructor(
    private http: HttpClient,
    @Inject('API_BASE_URL') private baseUrl: string
  ) {}

  login(username: string, password: string) {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/auth/login`, { username, password }, { headers, observe: 'response' })
      .pipe(
        map((resp) => {
          const data = resp.body || {};
          if (!data.authenticated) throw new Error('Invalid credentials');

          if (data.token) localStorage.setItem('auth_token', data.token);
          localStorage.setItem('auth_flag', '1');

          // store name and id
          if (data.user?.name) localStorage.setItem('user_name', data.user.name);
          if (data.user?.id != null) localStorage.setItem('user_id', String(data.user.id));

          this._isLoggedIn.next(true);
          this._currentUser.next(this.readUserFromStorage());
          return true;
        }),
        catchError((err: HttpErrorResponse) => {
          const msg = err.error?.message || err.message || 'Login failed';
          return throwError(() => new Error(msg));
        })
      );
  }

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_flag');
    localStorage.removeItem('user_name');
    localStorage.removeItem('user_id');
    this._isLoggedIn.next(false);
    this._currentUser.next({ id: null, name: 'User' });
  }

  get token(): string | null { return localStorage.getItem('auth_token'); }
  get userName(): string { return localStorage.getItem('user_name') || 'User'; }
  get userId(): number | null {
    const raw = localStorage.getItem('user_id');
    const n = raw == null ? NaN : Number(raw);
    return Number.isFinite(n) ? n : null;
  }
  isLoggedInSync(): boolean { return this._isLoggedIn.value; }

  private hasSession(): boolean {
    return !!localStorage.getItem('auth_token') || !!localStorage.getItem('auth_flag');
  }

  private readUserFromStorage(): AppUser {
    const name = localStorage.getItem('user_name') || 'User';
    const raw = localStorage.getItem('user_id');
    const id = raw != null && Number.isFinite(Number(raw)) ? Number(raw) : null;
    return { id, name };
  }
}