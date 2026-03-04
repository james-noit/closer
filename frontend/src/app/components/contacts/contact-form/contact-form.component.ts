import { Component, Inject, OnInit, Optional } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ContactService } from '../../../services/contact.service';
import { Contact } from '../../../models/contact.model';

@Component({
  selector: 'app-contact-form',
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.scss']
})
export class ContactFormComponent implements OnInit {
  contactForm: FormGroup;
  isEdit = false;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<ContactFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { contact: Contact }
  ) {
    this.contactForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', Validators.email],
      phone: [''],
      birthday: [''],
      notes: [''],
      group: [1]
    });
  }

  ngOnInit(): void {
    if (this.data?.contact) {
      this.isEdit = true;
      this.contactForm.patchValue(this.data.contact);
    }
  }

  onSubmit(): void {
    if (this.contactForm.invalid) return;
    this.loading = true;
    const formValue = this.contactForm.value;
    const obs = this.isEdit
      ? this.contactService.updateContact(this.data.contact.id, formValue)
      : this.contactService.addContact(formValue);
    obs.subscribe({
      next: () => {
        this.snackBar.open(this.isEdit ? 'Contact updated!' : 'Contact added!', 'Close', { duration: 2000 });
        this.dialogRef.close(true);
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Error saving contact', 'Close', { duration: 3000 });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
