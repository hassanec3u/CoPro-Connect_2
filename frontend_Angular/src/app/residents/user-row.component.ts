import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Resident } from '../models';
import { getHappixTypeLabel } from '../utils/happix.utils';

@Component({
  selector: 'tr[app-user-row]',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-row.component.html',
  styleUrl: './user-row.component.css'
})
export class UserRowComponent {
  @Input() user!: Resident;
  @Output() edit = new EventEmitter<Resident>();
  @Output() delete = new EventEmitter<string>();

  getHappixTypeLabel = getHappixTypeLabel;
  // Angular templates n'exposent pas Math.* par défaut
  Math = Math;

  get isBailleur(): boolean {
    return this.user.statut_lot?.includes('Bailleur') || false;
  }

  get statusChipClass(): string {
    return this.isBailleur ? 'chip-bailleur' : 'chip-resident';
  }

  get statusLabel(): string {
    return this.isBailleur ? 'Bailleur' : 'Résident';
  }

  get rowClass(): string {
    return this.isBailleur ? 'row-bailleur' : 'row-resident';
  }

  getOccupantTooltip(): string {
    return this.user.occupants.slice(2).map((o) => o.nom).join(', ');
  }

  getHappixTooltip(): string {
    return (this.user.happix_accounts || []).slice(2).map((h) => h.nom).join(', ');
  }
}
