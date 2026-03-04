export type ReminderType = 'BIRTHDAY' | 'APPOINTMENT' | 'CUSTOM';

export interface Reminder {
  id: number;
  userId: number;
  contactId?: number;
  contactName?: string;
  type: ReminderType;
  title: string;
  description?: string;
  dueDate: string;
  completed: boolean;
  createdAt?: string;
}
