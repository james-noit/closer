import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactService } from '../../../services/contact.service';

@Component({
  selector: 'app-contact-import',
  templateUrl: './contact-import.component.html',
  styleUrls: ['./contact-import.component.scss']
})
export class ContactImportComponent {
  importData = '';
  loading = false;

  constructor(
    private contactService: ContactService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<ContactImportComponent>
  ) {}

  onImport(): void {
    if (!this.importData.trim()) return;
    this.loading = true;
    this.contactService.importContacts(this.importData).subscribe({
      next: (contacts) => {
        this.snackBar.open(`Imported ${contacts.length} contacts!`, 'Close', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Error importing contacts', 'Close', { duration: 3000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
