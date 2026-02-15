import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { ResidentsStoreService, PaginationState } from '../residents/residents-store.service';
import { Resident } from '../models';
import { ToastService } from '../core/toast.service';
import { SidebarService } from '../core/sidebar.service';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';
import { ToolbarComponent } from '../residents/toolbar.component';
import { PaginationComponent } from '../shared/ui/pagination.component';
import { EditModalComponent } from '../residents/edit-modal.component';
import { AddUserFormComponent } from '../residents/add-user-form.component';
import { UserRowComponent } from '../residents/user-row.component';
import { ExportPdfButtonComponent } from '../shared/export/export-pdf-button.component';
import { ConfirmDialogComponent } from '../shared/ui/confirm-dialog.component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarComponent,
    PaginationComponent,
    EditModalComponent,
    AddUserFormComponent,
    UserRowComponent,
    ExportPdfButtonComponent,
    ConfirmDialogComponent
  ],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  users: Resident[] = [];
  totalPages = 0;
  currentPage = 1;
  rowsPerPage = 10;
  
  searchTerm = '';
  filterBat = 'Tous';
  filterStatus = 'Tous';
  sortField = 'batiment';
  sortDirection: 'asc' | 'desc' = 'asc';

  editingUser: Resident | null = null;
  isEditModalOpen = false;
  isAddModalOpen = false;
  confirmDialog: { message: string; onConfirm: () => void; confirmText: string; cancelText: string } | null = null;

  private sub?: Subscription;

  constructor(
    private store: ResidentsStoreService,
    private toast: ToastService,
    private sidebar: SidebarService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // S'abonner aux données paginées
    this.sub = this.store.pagination$.subscribe((state: PaginationState) => {
      this.users = [...state.residents];
      this.currentPage = state.currentPage + 1; // Backend utilise 0-indexé, frontend 1-indexé
      this.totalPages = state.totalPages;
      this.rowsPerPage = state.pageSize;
      this.cdr.markForCheck();
    });
    
    // Charger la première page
    setTimeout(() => {
      this.loadData();
    }, 0);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  openSidebar(): void {
    this.sidebar.open();
  }
  
  private loadData(): void {
    this.store.loadResidents(
      this.currentPage - 1,
      this.rowsPerPage,
      this.searchTerm,
      this.filterBat,
      this.filterStatus,
      this.sortField,
      this.sortDirection
    );
  }

  handleSearch(value: string): void {
    this.searchTerm = value;
    this.currentPage = 1;
    this.loadData();
  }

  handleFilterBat(value: string): void {
    this.filterBat = value;
    this.currentPage = 1;
    this.loadData();
  }

  handleFilterStatus(value: string): void {
    this.filterStatus = value;
    this.currentPage = 1;
    this.loadData();
  }

  handleSort(field: string): void {
    if (this.sortField === field) {
      // Inverser la direction si on clique sur la même colonne
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      // Nouvelle colonne : trier en ascendant
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.currentPage = 1;
    this.loadData();
  }

  getSortIcon(field: string): string {
    if (this.sortField !== field) {
      return '⇅'; // Icône neutre
    }
    return this.sortDirection === 'asc' ? '▲' : '▼';
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadData();
  }

  handleDelete(id: string | undefined): void {
    if (!id) {
      this.toast.show('Erreur : identifiant manquant', 'error');
      return;
    }
    
    if (this.confirmDialog) {
      return;
    }
    
    this.confirmDialog = {
      message: 'Supprimer ce lot ?',
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      onConfirm: async () => {
        try {
          await this.store.deleteResident(String(id));
          this.loadData(); // Recharger après suppression
          this.cdr.detectChanges();
          this.toast.show('Lot supprimé avec succès !', 'success');
        } catch (err: any) {
          const message = getUserFriendlyErrorMessage(err);
          this.toast.show(message, 'error');
        } finally {
          this.confirmDialog = null;
          this.cdr.detectChanges();
        }
      }
    };
  }

  handleEdit(user: Resident): void {
    this.editingUser = JSON.parse(JSON.stringify(user));
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.editingUser = null;
  }

  async handleSaveEdit(updated: Resident): Promise<void> {
    try {
      await this.store.updateResident(updated);
      this.closeEditModal();
      this.loadData(); // Recharger après modification
      this.cdr.detectChanges();
      this.toast.show('Modifications enregistrées !', 'success');
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.toast.show(message, 'error');
    }
  }

  async handleAddUser(user: Resident): Promise<void> {
    try {
      await this.store.addResident(user);
      this.currentPage = 1;
      this.isAddModalOpen = false;
      this.loadData(); // Recharger après ajout
      this.cdr.detectChanges();
      this.toast.show('Lot ajouté avec succès !', 'success');
    } catch (err: any) {
      const message = getUserFriendlyErrorMessage(err);
      this.toast.show(message, 'error');
    }
  }
}
