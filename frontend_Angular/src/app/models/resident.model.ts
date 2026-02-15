import { HappixAccount } from './happix.model';

export interface Occupant {
  nom: string;
  mobile?: string;
  email?: string;
  _uid?: string;
}

export interface Resident {
  id?: string;
  lot_id: string;
  batiment: string;
  etage: string;
  porte: string;
  cave_id?: string;
  statut_lot?: string;
  proprietaire_nom: string;
  proprietaire_mobile?: string;
  proprietaire_email?: string;
  occupants: Occupant[];
  happix_accounts: HappixAccount[];
}
