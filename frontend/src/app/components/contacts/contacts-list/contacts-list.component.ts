import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactService } from '../../../services/contact.service';
import { Contact, GROUP_COLORS, GROUP_LABELS } from '../../../models/contact.model';
import { ContactFormComponent } from '../contact-form/contact-form.component';
import { ContactImportComponent } from '../contact-import/contact-import.component';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: 'app-contacts-list',
  templateUrl: './contacts-list.component.html',
  styleUrls: ['./contacts-list.component.scss']
})
export class ContactsListComponent implements OnInit {
  contacts: Contact[] = [];
  filteredContacts: Contact[] = [];
  loading = true;
  searchQuery = '';
  selectedGroup: number | null = null;
  groupColors = GROUP_COLORS;
  groupLabels = GROUP_LABELS;
  private searchSubject = new Subject<string>();

  constructor(
    private contactService: ContactService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.selectedGroup = params['group'] ? parseInt(params['group']) : null;
      this.loadContacts();
    });
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged()).subscribe(q => {
      this.filterContacts(q);
    });
  }

  loadContacts(): void {
    this.loading = true;
    this.contactService.getContacts(this.selectedGroup || undefined).subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.filteredContacts = contacts;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.contacts = this.getMockContacts();
        this.filteredContacts = this.contacts;
      }
    });
  }

  onSearch(query: string): void {
    this.searchSubject.next(query);
  }

  filterContacts(query: string): void {
    if (!query) {
      this.filteredContacts = this.contacts;
      return;
    }
    const q = query.toLowerCase();
    this.filteredContacts = this.contacts.filter(c =>
      c.name.toLowerCase().includes(q) || c.email?.toLowerCase().includes(q) || c.phone?.includes(q)
    );
  }

  openAddContact(): void {
    const dialogRef = this.dialog.open(ContactFormComponent, { width: '500px', maxWidth: '95vw' });
    dialogRef.afterClosed().subscribe(result => { if (result) this.loadContacts(); });
  }

  openEditContact(contact: Contact): void {
    const dialogRef = this.dialog.open(ContactFormComponent, {
      width: '500px', maxWidth: '95vw', data: { contact }
    });
    dialogRef.afterClosed().subscribe(result => { if (result) this.loadContacts(); });
  }

  openImport(): void {
    const dialogRef = this.dialog.open(ContactImportComponent, { width: '500px', maxWidth: '95vw' });
    dialogRef.afterClosed().subscribe(result => { if (result) this.loadContacts(); });
  }

  deleteContact(contact: Contact): void {
    if (!confirm(`Delete ${contact.name}?`)) return;
    this.contactService.deleteContact(contact.id).subscribe({
      next: () => {
        this.snackBar.open('Contact deleted', 'Close', { duration: 2000 });
        this.loadContacts();
      },
      error: () => this.snackBar.open('Error deleting contact', 'Close', { duration: 3000 })
    });
  }

  viewContact(id: number): void {
    this.router.navigate(['/contacts', id]);
  }

  messageContact(contact: Contact): void {
    this.router.navigate(['/messages', contact.id]);
  }

  filterByGroup(group: number | null): void {
    this.selectedGroup = group;
    this.router.navigate([], { queryParams: group ? { group } : {} });
  }

  getGroupStyle(group: number): { [key: string]: string } {
    return { 'background-color': GROUP_COLORS[group], color: group === 3 ? '#333' : '#fff' };
  }

  getMockContacts(): Contact[] {
    return [
      { id: 1, userId: 1, name: 'Alice Johnson', email: 'alice@example.com', phone: '+1234567890', group: 1, lastInteraction: new Date().toISOString() },
      { id: 2, userId: 1, name: 'Bob Smith', email: 'bob@example.com', group: 2, lastInteraction: new Date(Date.now() - 20 * 86400000).toISOString() },
      { id: 3, userId: 1, name: 'Carol Williams', email: 'carol@example.com', group: 3, lastInteraction: new Date(Date.now() - 60 * 86400000).toISOString() },
      { id: 4, userId: 1, name: 'David Brown', email: 'david@example.com', group: 4, lastInteraction: new Date(Date.now() - 120 * 86400000).toISOString() },
      { id: 5, userId: 1, name: 'Eve Davis', email: 'eve@example.com', group: 5, lastInteraction: new Date(Date.now() - 300 * 86400000).toISOString() }
    ];
  }
}
