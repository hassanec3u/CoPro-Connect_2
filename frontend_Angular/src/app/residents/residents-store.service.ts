import { Injectable } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { Resident } from '../models';
import { ResidentService, PagedResponse } from './resident.service';
import { AuthService } from '../core/auth.service';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';

export interface PaginationState {
  residents: Resident[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}

@Injectable({ providedIn: 'root' })
export class ResidentsStoreService {
  private paginationSubject = new BehaviorSubject<PaginationState>({
    residents: [],
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 10
  });
  private residentsSubject = new BehaviorSubject<Resident[]>([]);
  private errorSubject = new BehaviorSubject<string | null>(null);
  private authSub?: Subscription;

  pagination$ = this.paginationSubject.asObservable();
  residents$ = this.residentsSubject.asObservable();
  error$ = this.errorSubject.asObservable();

  constructor(private residentService: ResidentService, private auth: AuthService) {
    // Charger les résidents si un token existe déjà
    const currentToken = this.auth.token;
    if (currentToken) {
      this.loadResidents(0, 10);
    }
    
    // Écouter les changements de token
    this.authSub = this.auth.token$.subscribe((token) => {
      if (!token) {
        this.paginationSubject.next({
          residents: [],
          currentPage: 0,
          totalPages: 0,
          totalElements: 0,
          pageSize: 10
        });
        this.residentsSubject.next([]);
        return;
      }
      // Charger les résidents quand un nouveau token est émis
      this.loadResidents(0, 10);
    });
  }

  async loadResidents(
    page: number = 0, 
    size: number = 10, 
    search?: string, 
    batiment?: string, 
    statutLot?: string,
    sortField?: string,
    sortDirection?: 'asc' | 'desc'
  ): Promise<void> {
    this.errorSubject.next(null);
    try {
      const response = await this.residentService.getResidentsPaginated(
        page, 
        size, 
        search, 
        batiment, 
        statutLot,
        sortField,
        sortDirection
      );
      this.paginationSubject.next({
        residents: response.residents || [],
        currentPage: response.currentPage,
        totalPages: response.totalPages,
        totalElements: response.totalElements,
        pageSize: response.pageSize
      });
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.errorSubject.next(message);
      if (message.toLowerCase().includes('expir') || message.toLowerCase().includes('session')) {
        this.auth.logout(false);
      }
      // En cas d'erreur, s'assurer que la liste est vide
      this.paginationSubject.next({
        residents: [],
        currentPage: 0,
        totalPages: 0,
        totalElements: 0,
        pageSize: 10
      });
    }
  }

  async loadAllResidents(): Promise<void> {
    this.errorSubject.next(null);
    try {
      const residents = await this.residentService.getResidents();
      this.residentsSubject.next(residents || []);
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.errorSubject.next(message);
      if (message.toLowerCase().includes('expir') || message.toLowerCase().includes('session')) {
        this.auth.logout(false);
      }
      this.residentsSubject.next([]);
    }
  }

  async addResident(resident: Resident): Promise<Resident> {
    try {
      // Créer d'abord dans le backend
      const created = await this.residentService.createResident(resident);
      
      // Recharger la première page pour voir le nouvel élément
      const currentState = this.paginationSubject.value;
      await this.loadResidents(0, currentState.pageSize);
      
      return created;
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      throw new Error(message);
    }
  }

  async updateResident(resident: Resident): Promise<Resident> {
    try {
      // Mettre à jour d'abord dans le backend
      const updated = await this.residentService.updateResident(resident);
      
      // Recharger la page courante pour voir les modifications
      const currentState = this.paginationSubject.value;
      await this.loadResidents(currentState.currentPage, currentState.pageSize);
      
      return updated;
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      throw new Error(message);
    }
  }

  async deleteResident(id: string): Promise<void> {
    try {
      // Supprimer d'abord du backend
      await this.residentService.deleteResident(id);
      
      // Recharger la page courante
      const currentState = this.paginationSubject.value;
      await this.loadResidents(currentState.currentPage, currentState.pageSize);
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      throw new Error(message);
    }
  }
}
