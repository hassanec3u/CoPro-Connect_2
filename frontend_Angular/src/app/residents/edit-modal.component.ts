import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Resident } from '../models';

interface FormErrors {
  lot_id?: string;
  batiment?: string;
  etage?: string;
  porte?: string;
  cave_id?: string;
  proprietaire_nom?: string;
}
import { BATIMENTS, STATUTS } from '../utils';
import { HAPPIX_RELATIONS, HAPPIX_TYPES, getHappixTypeLabel, getHappixChipClass } from '../utils/happix.utils';

@Component({
  selector: 'app-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-modal.component.html',
  styleUrl: './edit-modal.component.css'
})
export class EditModalComponent implements OnChanges {
  @Input() user: Resident | null = null;
  @Output() save = new EventEmitter<Resident>();
  @Output() close = new EventEmitter<void>();

  localUser: Resident | null = null;
  errors: FormErrors = {};
  batiments = BATIMENTS;
  statuts = STATUTS;
  happixTypes = HAPPIX_TYPES;
  happixRelations = HAPPIX_RELATIONS;

  getHappixTypeLabel = getHappixTypeLabel;
  getHappixChipClass = getHappixChipClass;

  ngOnChanges(): void {
    this.localUser = this.user ? JSON.parse(JSON.stringify(this.user)) : null;
    this.errors = {};
  }

  addOccupant(): void {
    if (!this.localUser) return;
    this.localUser.occupants = [
      ...(this.localUser.occupants || []),
      { _uid: `uid_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`, nom: '', mobile: '', email: '' }
    ];
  }

  removeOccupant(index: number): void {
    if (!this.localUser) return;
    this.localUser.occupants = this.localUser.occupants.filter((_, i) => i !== index);
  }

  addHappix(): void {
    if (!this.localUser) return;
    this.localUser.happix_accounts = [
      ...(this.localUser.happix_accounts || []),
      {
        _uid: `uid_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`,
        id: Date.now(),
        nom: '',
        mobile: '',
        email: '',
        type: 'resident',
        relation: 'occupant',
        nom_borne: ''
      }
    ];
  }

  removeHappix(index: number): void {
    if (!this.localUser) return;
    this.localUser.happix_accounts = this.localUser.happix_accounts.filter((_, i) => i !== index);
  }

  validateForm(): boolean {
    if (!this.localUser) return false;
    const errors: FormErrors = {};

    if (!this.localUser.lot_id?.trim()) {
      errors.lot_id = 'Le numéro de lot est obligatoire';
    } else if (!/^\d+$/.test(String(this.localUser.lot_id).trim())) {
      errors.lot_id = 'Le numéro de lot doit être un nombre';
    }

    if (!this.localUser.batiment?.trim()) {
      errors.batiment = 'Le bâtiment est obligatoire';
    }

    if (!this.localUser.etage?.trim()) {
      errors.etage = "L'étage est obligatoire";
    }

    if (!this.localUser.porte?.trim()) {
      errors.porte = "L'appartement est obligatoire";
    }

    if (!this.localUser.proprietaire_nom?.trim()) {
      errors.proprietaire_nom = 'Le nom du propriétaire est obligatoire';
    }

    if (this.localUser.cave_id?.trim() && !/^\d+$/.test(String(this.localUser.cave_id).trim())) {
      errors.cave_id = 'La cave doit être un nombre';
    }

    this.errors = errors;
    return Object.keys(errors).length === 0;
  }

  handleSave(): void {
    if (!this.validateForm() || !this.localUser) return;
    this.save.emit(this.localUser);
  }
}
