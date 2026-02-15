export const HAPPIX_TYPES = ['resident', 'autorisé'];

export const HAPPIX_RELATIONS = [
  'occupant',
  'propriétaire',
  'famille',
  'ami',
  'livraison',
  'service',
  'aide à domicile',
  'autre'
];

export const getHappixTypeLabel = (type?: string) =>
  type === 'resident' ? 'Résident' : 'Autorisé';

export const getHappixChipClass = (type?: string) =>
  type === 'resident' ? 'chip-happix' : 'chip-invite';
