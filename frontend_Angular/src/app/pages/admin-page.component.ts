import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, AdminUser } from '../core/admin.service';
import { AuthService } from '../core/auth.service';
import { SidebarService } from '../core/sidebar.service';
import { ToastService } from '../core/toast.service';
import { getUserFriendlyErrorMessage } from '../utils/error.utils';
import { ConfirmDialogComponent } from '../shared/ui/confirm-dialog.component';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ConfirmDialogComponent],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.css'
})
export class AdminPageComponent implements OnInit {
  users: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  loading = true;
  error: string | null = null;
  searchTerm = '';
  updatingIds = new Set<string>();

  confirmDialog: { message: string; onConfirm: () => void; confirmText: string; cancelText: string } | null = null;

  constructor(
    private readonly adminService: AdminService,
    private readonly auth: AuthService,
    private readonly sidebar: SidebarService,
    private readonly toast: ToastService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    void this.loadUsers();
  }

  openSidebar(): void {
    this.sidebar.open();
  }

  trackById(_: number, user: AdminUser): string {
    return user.id;
  }

  async loadUsers(): Promise<void> {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();
    try {
      this.users = await this.adminService.listUsers();
      this.applyFilter();
    } catch (err: any) {
      this.error = getUserFriendlyErrorMessage(err);
      this.toast.show(this.error, 'error');
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  onSearchChange(value: string): void {
    this.searchTerm = value;
    this.applyFilter();
    this.cdr.detectChanges();
  }

  private applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      this.filteredUsers = [...this.users];
      return;
    }
    this.filteredUsers = this.users.filter((u) =>
      (u.username || '').toLowerCase().includes(term) ||
      (u.name || '').toLowerCase().includes(term) ||
      (u.email || '').toLowerCase().includes(term) ||
      (u.role || '').toLowerCase().includes(term)
    );
  }

  isCurrentUser(user: AdminUser): boolean {
    const current = this.auth.currentUser;
    return !!current && current.id === user.id;
  }

  onToggleMfa(user: AdminUser): void {
    const nextValue = !user.mfaEnabled;
    const action = nextValue ? 'activer' : 'désactiver';
    const warning = this.isCurrentUser(user) && !nextValue
      ? ' Attention : vous désactivez le MFA sur votre propre compte.'
      : '';

    this.confirmDialog = {
      message: `Souhaitez-vous ${action} le MFA pour « ${user.username} » ?${warning}`,
      confirmText: nextValue ? 'Activer' : 'Désactiver',
      cancelText: 'Annuler',
      onConfirm: () => this.applyMfaToggle(user, nextValue)
    };
  }

  cancelConfirm(): void {
    this.confirmDialog = null;
    this.cdr.detectChanges();
  }

  private async applyMfaToggle(user: AdminUser, nextValue: boolean): Promise<void> {
    this.confirmDialog = null;
    this.updatingIds.add(user.id);
    this.cdr.detectChanges();

    try {
      const updated = await this.adminService.updateMfa(user.id, nextValue);
      const idx = this.users.findIndex((u) => u.id === user.id);
      if (idx >= 0) {
        this.users[idx] = { ...this.users[idx], ...updated };
      }
      this.applyFilter();
      const label = updated.mfaEnabled ? 'activé' : 'désactivé';
      this.toast.show(`MFA ${label} pour « ${user.username} »`, 'success');
    } catch (err: any) {
      const msg = getUserFriendlyErrorMessage(err);
      this.toast.show(msg, 'error');
    } finally {
      this.updatingIds.delete(user.id);
      this.cdr.detectChanges();
    }
  }

  isUpdating(user: AdminUser): boolean {
    return this.updatingIds.has(user.id);
  }

  formatDate(value?: string): string {
    if (!value) return '—';
    try {
      return new Date(value).toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch {
      return value;
    }
  }
}
