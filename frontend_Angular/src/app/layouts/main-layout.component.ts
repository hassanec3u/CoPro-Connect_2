import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../shared/ui/sidebar.component';
import { AuthService } from '../core/auth.service';
import { SidebarService } from '../core/sidebar.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  sidebarOpen = true; // Toujours ouvert sur desktop

  constructor(
    public router: Router,
    private auth: AuthService,
    private sidebarService: SidebarService
  ) {
    // Sur desktop, sidebar toujours ouverte
    if (typeof window !== 'undefined' && window.innerWidth > 768) {
      this.sidebarOpen = true;
      this.sidebarService.open();
    }
    
    this.sidebarService.isOpen$.subscribe((open) => {
      // Ne pas fermer sur desktop
      if (typeof window !== 'undefined' && window.innerWidth > 768) {
        this.sidebarOpen = true;
      } else {
        this.sidebarOpen = open;
      }
    });
  }

  handleNavigate(path: string): void {
    this.router.navigateByUrl(path).then(() => {
      // Forcer le rechargement des données après navigation
      // Le composant de destination se chargera automatiquement via ngOnInit
    });
  }

  handleLogout(): void {
    this.auth.logout(true);
    this.router.navigateByUrl('/login');
    this.sidebarService.close();
  }

  closeSidebar(): void {
    this.sidebarService.close();
  }

  isMobile(): boolean {
    return window.innerWidth <= 768;
  }
}
