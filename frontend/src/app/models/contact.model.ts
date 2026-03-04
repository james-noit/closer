export interface Contact {
  id: number;
  userId: number;
  name: string;
  email?: string;
  phone?: string;
  birthday?: string;
  notes?: string;
  group: 1 | 2 | 3 | 4 | 5;
  lastInteraction?: string;
  createdAt?: string;
  updatedAt?: string;
}

export const GROUP_COLORS: Record<number, string> = {
  1: '#4CAF50',
  2: '#8BC34A',
  3: '#FFC107',
  4: '#FF9800',
  5: '#F44336'
};

export const GROUP_LABELS: Record<number, string> = {
  1: 'Recent',
  2: 'Monthly',
  3: '3 Months',
  4: '6 Months',
  5: 'Overdue'
};
