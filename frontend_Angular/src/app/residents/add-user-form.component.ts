import { Component, EventEmitter, Output } from '@angular/core';
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

const createInitialUser = (): Resident => ({
  lot_id: '',
  batiment: '',
  etage: '',
  porte: '',
  cave_id: '',
  statut_lot: 'Propriétaire Résident',
  proprietaire_nom: '',
  proprietaire_mobile: '',
  proprietaire_email: '',
  occupants: [],
  happix_accounts: []
});

@Component({
  selector: 'app-add-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-user-form.component.html',
  styleUrl: './add-user-form.component.css'
})
export class AddUserFormComponent {
  @Output() add = new EventEmitter<Resident>();

  newUser: Resident = createInitialUser();
  errors: FormErrors = {};
  batiments = BATIMENTS;
  statuts = STATUTS;

  addOccupant(): void {
    this.newUser.occupants = [...this.newUser.occupants, { nom: '', mobile: '', email: '' }];
  }

  removeOccupant(index: number): void {
    this.newUser.occupants = this.newUser.occupants.filter((_, i) => i !== index);
  }

  validateForm(): boolean {
    const errors: FormErrors = {};

    if (!this.newUser.lot_id?.trim()) {
      errors.lot_id = 'Le numéro de lot est obligatoire';
    } else if (!/^\d+$/.test(this.newUser.lot_id.trim())) {
      errors.lot_id = 'Le numéro de lot doit être un nombre';
    }

    if (!this.newUser.batiment?.trim()) {
      errors.batiment = 'Le bâtiment est obligatoire';
    }

    if (!this.newUser.etage?.trim()) {
      errors.etage = "L'étage est obligatoire";
    }

    if (!this.newUser.porte?.trim()) {
      errors.porte = "L'appartement est obligatoire";
    }

    if (!this.newUser.proprietaire_nom?.trim()) {
      errors.proprietaire_nom = 'Le nom du propriétaire est obligatoire';
    }

    if (this.newUser.cave_id?.trim() && !/^\d+$/.test(this.newUser.cave_id.trim())) {
      errors.cave_id = 'La cave doit être un nombre';
    }

    this.errors = errors;
    return Object.keys(errors).length === 0;
  }

  handleSubmit(): void {
    if (!this.validateForm()) {
      return;
    }

    this.add.emit({ ...this.newUser });
    this.newUser = createInitialUser();
    this.errors = {};
  }
}
