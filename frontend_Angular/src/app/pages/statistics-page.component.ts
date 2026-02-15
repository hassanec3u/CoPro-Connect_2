import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResidentService, StatisticsResponse } from '../residents/resident.service';
import { SidebarService } from '../core/sidebar.service';

@Component({
  selector: 'app-statistics-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistics-page.component.html',
  styleUrl: './statistics-page.component.css'
})
export class StatisticsPageComponent implements OnInit {
  stats: StatisticsResponse | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private residentService: ResidentService, 
    private sidebar: SidebarService,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit(): Promise<void> {
    await this.loadStatistics();
  }

  async loadStatistics(): Promise<void> {
    try {
      this.loading = true;
      this.error = null;
      this.cdr.detectChanges();
      
      this.stats = await this.residentService.getStatistics();
      console.log('Statistiques chargées:', this.stats);
    } catch (err: any) {
      this.error = err.message || 'Erreur lors du chargement des statistiques';
      console.error('Erreur lors du chargement des statistiques:', err);
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  openSidebar(): void {
    this.sidebar.open();
  }
}
