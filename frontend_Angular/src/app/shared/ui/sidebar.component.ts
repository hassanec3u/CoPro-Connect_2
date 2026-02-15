import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  @Input() currentPath = '/';
  @Input() isOpen = false;
  @Output() navigateTo = new EventEmitter<string>();
  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  menuItems = [
    { path: '/', label: '👥 Gestion Résidents' },
    { path: '/happix', label: '🔑 Comptes Happix' },
    { path: '/statistiques', label: '📊 Statistiques' }
  ];

  handleNavigate(path: string): void {
    this.navigateTo.emit(path);
    this.close.emit();
  }
}
