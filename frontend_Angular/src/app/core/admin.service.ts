import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, firstValueFrom, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';

const API_BASE = environment.apiUrl.replace('/api', '');

export interface AdminUser {
  id: string;
  username: string;
  name?: string;
  email?: string;
  role: string;
  mfaEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private readonly http: HttpClient) {}

  async listUsers(): Promise<AdminUser[]> {
    return firstValueFrom(
      this.http
        .get<AdminUser[]>(`${API_BASE}/api/admin/users`)
        .pipe(catchError((err) => this.handleError(err)))
    );
  }

  async updateMfa(userId: string, mfaEnabled: boolean): Promise<AdminUser> {
    return firstValueFrom(
      this.http
        .patch<AdminUser>(`${API_BASE}/api/admin/users/${userId}/mfa`, { mfaEnabled })
        .pipe(catchError((err) => this.handleError(err)))
    );
  }

  private handleError(error: HttpErrorResponse) {
    const msg = getUserFriendlyErrorMessage(error);
    return throwError(() => new Error(msg));
  }
}
