import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Resident } from '../../models';
import { getHappixTypeLabel } from '../../utils/happix.utils';
import { ExportHappixPdfButtonComponent } from '../../shared/export/export-happix-pdf-button.component';

interface HappixEntry {
  id: string;
  nom: string;
  email: string;
  mobile: string;
  nom_borne?: string | null;
  type: string;
  relation: string;
  batiment: string;
  appart: string;
  proprietaire: string;
  residents: string[];
}

@Component({
  selector: 'app-happix-list',
  standalone: true,
  imports: [CommonModule, ExportHappixPdfButtonComponent],
  templateUrl: './happix-list.component.html',
  styleUrl: './happix-list.component.css'
})
export class HappixListComponent {
  @Input() users: Resident[] = [];
  @Input() showBack = true;
  @Output() back = new EventEmitter<void>();

  sortColumn: keyof HappixEntry | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';

  getHappixTypeLabel = getHappixTypeLabel;

  handleSort(column: keyof HappixEntry): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
  }

  get entries(): HappixEntry[] {
    return this.users.flatMap((user) =>
      (user.happix_accounts || []).map((h, idx) => ({
        id: `${user.id || 'u'}-h-${idx}`,
        nom: h.nom || '-',
        email: h.email || '-',
        mobile: h.mobile || user.proprietaire_mobile || '-',
        nom_borne: h.nom_borne || null,
        type: h.type || '-',
        relation: h.relation || '-',
        batiment: user.batiment || '-',
        appart: user.porte || '-',
        proprietaire: user.proprietaire_nom || '-',
        residents: [
          ...(user.proprietaire_nom ? [user.proprietaire_nom] : []),
          ...((user.occupants || []).map((o) => o.nom).filter(Boolean))
        ].filter((v, i, a) => a.indexOf(v) === i)
      }))
    );
  }

  get sortedEntries(): HappixEntry[] {
    if (!this.sortColumn) return [...this.entries];
    const entries = [...this.entries];
    entries.sort((a, b) => {
      const aValue = a[this.sortColumn || 'nom'];
      const bValue = b[this.sortColumn || 'nom'];
      if (this.sortColumn === 'residents') {
        const diff = (aValue as string[]).length - (bValue as string[]).length;
        return this.sortDirection === 'asc' ? diff : -diff;
      }
      const comp = String(aValue).localeCompare(String(bValue));
      return this.sortDirection === 'asc' ? comp : -comp;
    });
    return entries;
  }
}
