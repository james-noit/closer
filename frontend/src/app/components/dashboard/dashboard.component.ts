import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactService } from '../../services/contact.service';
import { DashboardData } from '../../models/dashboard.model';
import { ContactFormComponent } from '../contacts/contact-form/contact-form.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  dashboardData: DashboardData | null = null;
  loading = true;

  mockData: DashboardData = {
    totalContacts: 47,
    contactLimit: 150,
    groups: [
      { group: 1, label: 'Recent', color: '#4CAF50', count: 2, percentage: 4.3 },
      { group: 2, label: 'Monthly', color: '#8BC34A', count: 4, percentage: 8.5 },
      { group: 3, label: '3 Months', color: '#FFC107', count: 10, percentage: 21.3 },
      { group: 4, label: '6 Months', color: '#FF9800', count: 15, percentage: 31.9 },
      { group: 5, label: 'Overdue', color: '#F44336', count: 16, percentage: 34.0 }
    ]
  };

  constructor(
    private contactService: ContactService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.contactService.getDashboard().subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.loading = false;
      },
      error: () => {
        this.dashboardData = this.mockData;
        this.loading = false;
      }
    });
  }

  goToGroup(group: number): void {
    this.router.navigate(['/contacts'], { queryParams: { group } });
  }

  openAddContact(): void {
    const dialogRef = this.dialog.open(ContactFormComponent, {
      width: '500px',
      maxWidth: '95vw'
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadDashboard();
    });
  }

  getGroupClass(group: number): string {
    return `group-${group}`;
  }
}
