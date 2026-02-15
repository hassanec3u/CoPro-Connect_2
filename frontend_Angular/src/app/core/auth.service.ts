import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject, interval, Subscription, firstValueFrom, catchError, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';
import { environment } from '../../environments/environment';

const API_BASE = environment.apiUrl.replace('/api', '');

interface LoginResponse {
  token?: string;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenSubject = new BehaviorSubject<string | null>(this.getStoredValidToken());
  private sessionExpiredSubject = new Subject<void>();
  private monitorSub?: Subscription;

  token$ = this.tokenSubject.asObservable();
  sessionExpired$ = this.sessionExpiredSubject.asObservable();

  constructor(private http: HttpClient) {
    const token = this.tokenSubject.value;
    if (token) {
      this.startTokenMonitor(token);
    }
  }

  async login(username: string, password: string): Promise<string> {
    try {
      const response = await firstValueFrom(
        this.http.post<LoginResponse>(`${API_BASE}/api/auth/login`, { username, password })
          .pipe(
            // Intercepter les erreurs HTTP avant qu'elles ne soient converties
            catchError((error: HttpErrorResponse) => {
              // Convertir immédiatement l'erreur HTTP en erreur utilisateur-friendly
              const userMessage = getUserFriendlyErrorMessage(error);
              return throwError(() => new Error(userMessage));
            })
          )
      );

      if (!response?.token) {
        throw new Error(response?.message || 'Erreur de connexion. Veuillez réessayer.');
      }

      if (!this.isTokenValid(response.token)) {
        throw new Error('Erreur d\'authentification. Veuillez réessayer.');
      }

      localStorage.setItem('token', response.token);
      this.tokenSubject.next(response.token);
      this.startTokenMonitor(response.token);
      return response.token;
    } catch (error: any) {
      // S'assurer que toutes les erreurs sont converties et propagées
      const userMessage = getUserFriendlyErrorMessage(error);
      throw new Error(userMessage);
    }
  }

  logout(triggeredByUser = true): void {
    localStorage.removeItem('token');
    this.tokenSubject.next(null);
    this.stopTokenMonitor();
    if (!triggeredByUser) {
      this.sessionExpiredSubject.next();
    }
  }

  get token(): string | null {
    return this.tokenSubject.value;
  }

  get isAuthenticated(): boolean {
    const token = this.tokenSubject.value;
    return !!token && this.isTokenValid(token);
  }

  private getStoredValidToken(): string | null {
    const stored = localStorage.getItem('token');
    if (!stored) return null;
    if (!this.isTokenValid(stored)) {
      localStorage.removeItem('token');
      return null;
    }
    return stored;
  }

  private startTokenMonitor(token: string): void {
    this.stopTokenMonitor();
    if (!this.isTokenValid(token)) {
      this.logout(false);
      return;
    }

    this.monitorSub = interval(60 * 1000).subscribe(() => {
      const current = this.tokenSubject.value;
      if (current && this.isTokenExpired(current)) {
        this.logout(false);
      }
    });
  }

  private stopTokenMonitor(): void {
    if (this.monitorSub) {
      this.monitorSub.unsubscribe();
      this.monitorSub = undefined;
    }
  }

  private decodeToken(token: string): any | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = parts[1];
      return JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded?.exp) return true;
    const expirationTime = decoded.exp * 1000;
    const margin = 5 * 60 * 1000;
    return Date.now() >= (expirationTime - margin);
  }

  private isTokenValid(token: string): boolean {
    return !this.isTokenExpired(token);
  }
}
