import { Component, EventEmitter, Input, Output, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { AuthService, AuthUser } from '../../core/auth.service';

interface MenuItem {
  path: string;
  label: string;
  adminOnly?: boolean;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() currentPath = '/';
  @Input() isOpen = false;
  @Output() navigateTo = new EventEmitter<string>();
  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  currentUser: AuthUser | null = null;
  private userSub?: Subscription;

  private readonly allMenuItems: MenuItem[] = [
    { path: '/', label: '👥 Gestion Résidents' },
    { path: '/happix', label: '🔑 Comptes Happix' },
    { path: '/statistiques', label: '📊 Statistiques' },
    { path: '/admin', label: '🛡️ Administration', adminOnly: true }
  ];

  constructor(private readonly auth: AuthService, private readonly cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.currentUser = this.auth.currentUser;
    this.userSub = this.auth.user$.subscribe((user) => {
      this.currentUser = user;
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy(): void {
    this.userSub?.unsubscribe();
  }

  get menuItems(): MenuItem[] {
    const isAdmin = (this.currentUser?.role || '').toUpperCase() === 'ADMIN';
    return this.allMenuItems.filter((item) => !item.adminOnly || isAdmin);
  }

  handleNavigate(path: string): void {
    this.navigateTo.emit(path);
    this.close.emit();
  }
}
