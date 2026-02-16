export interface ChangeDetail {
  category: 'LOT' | 'PROPRIETAIRE' | 'OCCUPANT' | 'HAPPIX';
  change_type: 'MODIFIED' | 'ADDED' | 'REMOVED';
  field_label: string;
  old_value: string | null;
  new_value: string | null;
}

export interface ResidentHistoryResponse {
  id: string;
  resident_id: string;
  lot_id: string;
  batiment: string;
  etage: string;
  porte: string;
  action_type: 'UPDATE' | 'DELETE';
  description: string;
  changes: ChangeDetail[];
  changed_at: string;
  changed_by?: string;
}
