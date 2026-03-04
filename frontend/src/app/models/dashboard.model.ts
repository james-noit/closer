export interface DashboardGroup {
  group: 1 | 2 | 3 | 4 | 5;
  label: string;
  color: string;
  count: number;
  percentage: number;
}

export interface DashboardData {
  totalContacts: number;
  contactLimit: number;
  groups: DashboardGroup[];
}
