import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { firstValueFrom, throwError } from 'rxjs';
import { Resident } from '../models';
import { ResidentHistoryResponse } from '../models/resident-history.model';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';
import { environment } from '../../environments/environment';

const API_BASE = environment.apiUrl.replace('/api', '');

export interface PagedResponse {
  residents: Resident[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}

export interface StatisticsResponse {
  totalLots: number;
  totalBatiments: number;
  totalOccupants: number;
  totalHappix: number;
  statutCount: Record<string, number>;
  batimentCount: Record<string, number>;
  lotsAvecOccupants: number;
  lotsVides: number;
  moyenneOccupants: number;
  happixByType: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class ResidentService {
  constructor(private http: HttpClient) {}

  async getResidents(): Promise<Resident[]> {
    const response = await firstValueFrom(
      this.http
        .get<any>(`${API_BASE}/api/residents/all`)
        .pipe(map((data) => this.normalizeResidents(this.extractData(data))))
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  async getResidentsPaginated(
    page: number, 
    size: number, 
    search?: string, 
    batiment?: string, 
    statutLot?: string,
    sortField?: string,
    sortDirection?: 'asc' | 'desc'
  ): Promise<PagedResponse> {
    // Construire les paramètres de requête
    let url = `${API_BASE}/api/residents?page=${page}&size=${size}`;
    if (search && search.trim()) {
      url += `&search=${encodeURIComponent(search.trim())}`;
    }
    if (batiment && batiment !== 'Tous') {
      url += `&batiment=${encodeURIComponent(batiment)}`;
    }
    if (statutLot && statutLot !== 'Tous') {
      url += `&statutLot=${encodeURIComponent(statutLot)}`;
    }
    if (sortField) {
      url += `&sort=${encodeURIComponent(sortField)},${sortDirection || 'asc'}`;
    }
    
    const response = await firstValueFrom(
      this.http
        .get<PagedResponse>(url)
        .pipe(
          map((data) => ({
            ...data,
            residents: this.normalizeResidents(data.residents)
          }))
        )
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  async getStatistics(): Promise<StatisticsResponse> {
    const response = await firstValueFrom(
      this.http
        .get<StatisticsResponse>(`${API_BASE}/api/residents/statistics`)
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  async createResident(resident: Resident): Promise<Resident> {
    const response = await firstValueFrom(
      this.http
        .post<any>(`${API_BASE}/api/residents`, resident)
        .pipe(map((data) => this.normalizeResident(data.data || data)))
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  async updateResident(resident: Resident): Promise<Resident> {
    if (!resident.id) {
      throw new Error('Identifiant manquant pour la mise a jour');
    }
    const response = await firstValueFrom(
      this.http
        .put<any>(`${API_BASE}/api/residents/${resident.id}`, resident)
        .pipe(map((data) => this.normalizeResident(data.data || data)))
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  async deleteResident(id: string): Promise<void> {
    await firstValueFrom(
      this.http
        .delete(`${API_BASE}/api/residents/${id}`)
        .pipe(catchError((err) => this.handleError(err)))
    );
  }

  /**
   * Récupère l'historique d'un appartement
   */
  async getApartmentHistory(batiment: string, etage: string, porte: string): Promise<ResidentHistoryResponse[]> {
    const url = `${API_BASE}/api/residents/history/apartment?batiment=${encodeURIComponent(batiment)}&etage=${encodeURIComponent(etage)}&porte=${encodeURIComponent(porte)}`;
    const response = await firstValueFrom(
      this.http
        .get<ResidentHistoryResponse[]>(url)
        .pipe(catchError((err) => this.handleError(err)))
    );
    return response;
  }

  /**
   * Télécharge le PDF des résidents depuis le backend
   */
  async exportResidentsPdf(): Promise<void> {
    const blob = await firstValueFrom(
      this.http
        .get(`${API_BASE}/api/export/residents/pdf`, { responseType: 'blob' })
        .pipe(catchError((err) => this.handleError(err)))
    );
    this.downloadBlob(blob, 'residents-list.pdf');
  }

  /**
   * Télécharge le PDF des comptes Happix depuis le backend
   */
  async exportHappixPdf(): Promise<void> {
    const blob = await firstValueFrom(
      this.http
        .get(`${API_BASE}/api/export/happix/pdf`, { responseType: 'blob' })
        .pipe(catchError((err) => this.handleError(err)))
    );
    this.downloadBlob(blob, 'happix-list.pdf');
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  private normalizeResident(data: any): Resident {
    return {
      ...data,
      occupants: data?.occupants || [],
      happix_accounts: data?.happix_accounts || []
    };
  }

  private normalizeResidents(data: any[]): Resident[] {
    return (data || []).map((item) => this.normalizeResident(item));
  }

  private extractData(response: any): any[] {
    if (Array.isArray(response)) return response;
    if (response?.data && Array.isArray(response.data)) return response.data;
    if (response?.data) return [response.data];
    if (response && typeof response === 'object') return [response];
    return [];
  }

  private handleError(error: HttpErrorResponse) {
    const userMessage = getUserFriendlyErrorMessage(error);
    return throwError(() => new Error(userMessage));
  }
}
