import { Component, EventEmitter, Input, OnChanges, OnInit, Output, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResidentHistoryResponse, ChangeDetail } from '../models/resident-history.model';
import { ResidentService } from './resident.service';

@Component({
  selector: 'app-history-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './history-modal.component.html',
  styleUrl: './history-modal.component.css'
})
export class HistoryModalComponent implements OnInit, OnChanges {
  @Input() apartment: { batiment: string; etage: string; porte: string } | null = null;
  @Output() close = new EventEmitter<void>();

  history: ResidentHistoryResponse[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private residentService: ResidentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  ngOnChanges(): void {
    this.loadHistory();
  }

  async loadHistory(): Promise<void> {
    if (!this.apartment) return;

    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    try {
      this.history = await this.residentService.getApartmentHistory(
        this.apartment.batiment,
        this.apartment.etage,
        this.apartment.porte
      );
    } catch (err: any) {
      this.error = err.message || 'Erreur lors du chargement de l\'historique';
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getChangeIcon(change: ChangeDetail): string {
    if (change.change_type === 'ADDED') return '+';
    if (change.change_type === 'REMOVED') return '−';
    return '~';
  }

  getChangeTypeClass(change: ChangeDetail): string {
    if (change.change_type === 'ADDED') return 'change-added';
    if (change.change_type === 'REMOVED') return 'change-removed';
    return 'change-modified';
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'LOT': return '🏠';
      case 'PROPRIETAIRE': return '👤';
      case 'OCCUPANT': return '👥';
      case 'HAPPIX': return '🔑';
      default: return '📝';
    }
  }

  getCategoryLabel(category: string): string {
    switch (category) {
      case 'LOT': return 'Lot';
      case 'PROPRIETAIRE': return 'Propriétaire';
      case 'OCCUPANT': return 'Occupant';
      case 'HAPPIX': return 'Compte Happix';
      default: return category;
    }
  }

  /**
   * Regroupe les changements par catégorie
   */
  groupByCategory(changes: ChangeDetail[]): { category: string; items: ChangeDetail[] }[] {
    const map = new Map<string, ChangeDetail[]>();
    const order = ['LOT', 'PROPRIETAIRE', 'OCCUPANT', 'HAPPIX'];
    
    for (const change of changes) {
      const cat = change.category;
      if (!map.has(cat)) {
        map.set(cat, []);
      }
      map.get(cat)!.push(change);
    }
    
    return order
      .filter(cat => map.has(cat))
      .map(cat => ({ category: cat, items: map.get(cat)! }));
  }
}
