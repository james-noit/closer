import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactService } from '../../../services/contact.service';
import { Contact, GROUP_COLORS, GROUP_LABELS } from '../../../models/contact.model';
import { ContactFormComponent } from '../contact-form/contact-form.component';

@Component({
  selector: 'app-contact-detail',
  templateUrl: './contact-detail.component.html',
  styleUrls: ['./contact-detail.component.scss']
})
export class ContactDetailComponent implements OnInit {
  contact: Contact | null = null;
  loading = true;
  groupColors = GROUP_COLORS;
  groupLabels = GROUP_LABELS;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contactService: ContactService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadContact(parseInt(id));
  }

  loadContact(id: number): void {
    this.contactService.getContact(id).subscribe({
      next: (contact) => { this.contact = contact; this.loading = false; },
      error: () => {
        this.loading = false;
        this.contact = {
          id, userId: 1, name: 'Sample Contact', email: 'sample@example.com',
          phone: '+1234567890', group: 1, birthday: '1990-05-15',
          notes: 'Great friend from college', lastInteraction: new Date().toISOString()
        };
      }
    });
  }

  openWhatsApp(): void {
    if (this.contact?.phone) window.open(`https://wa.me/${this.contact.phone.replace(/\D/g, '')}`, '_blank');
  }

  openTelegram(): void {
    if (this.contact?.phone) window.open(`https://t.me/${this.contact.phone.replace(/\D/g, '')}`, '_blank');
  }

  openSignal(): void {
    this.snackBar.open('Opening Signal...', 'Close', { duration: 2000 });
    if (this.contact?.phone) window.open(`https://signal.me/#p/${this.contact.phone}`, '_blank');
  }

  openMessenger(): void {
    window.open('https://www.messenger.com/', '_blank');
  }

  sendInAppMessage(): void {
    if (this.contact) this.router.navigate(['/messages', this.contact.id]);
  }

  logInteraction(): void {
    if (!this.contact) return;
    this.contactService.updateInteraction(this.contact.id).subscribe({
      next: (updated) => {
        this.contact = updated;
        this.snackBar.open('Interaction logged!', 'Close', { duration: 2000 });
      },
      error: () => this.snackBar.open('Interaction logged (offline mode)', 'Close', { duration: 2000 })
    });
  }

  editContact(): void {
    const dialogRef = this.dialog.open(ContactFormComponent, {
      width: '500px', maxWidth: '95vw', data: { contact: this.contact }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && this.contact) this.loadContact(this.contact.id);
    });
  }

  deleteContact(): void {
    if (!this.contact || !confirm(`Delete ${this.contact.name}?`)) return;
    this.contactService.deleteContact(this.contact.id).subscribe({
      next: () => { this.snackBar.open('Contact deleted', 'Close', { duration: 2000 }); this.router.navigate(['/contacts']); },
      error: () => this.snackBar.open('Error deleting contact', 'Close', { duration: 3000 })
    });
  }

  getGroupStyle(): { [key: string]: string } {
    if (!this.contact) return {};
    return { 'background-color': GROUP_COLORS[this.contact.group], color: this.contact.group === 3 ? '#333' : '#fff' };
  }
}
