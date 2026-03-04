import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReminderService } from '../../services/reminder.service';
import { Reminder, ReminderType } from '../../models/reminder.model';

@Component({
  selector: 'app-reminders',
  templateUrl: './reminders.component.html',
  styleUrls: ['./reminders.component.scss']
})
export class RemindersComponent implements OnInit {
  reminders: Reminder[] = [];
  loading = true;
  showForm = false;
  reminderForm: FormGroup;
  reminderTypes: ReminderType[] = ['BIRTHDAY', 'APPOINTMENT', 'CUSTOM'];

  constructor(
    private reminderService: ReminderService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.reminderForm = this.fb.group({
      title: ['', Validators.required],
      type: ['CUSTOM', Validators.required],
      dueDate: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadReminders();
  }

  loadReminders(): void {
    this.reminderService.getReminders().subscribe({
      next: (r) => { this.reminders = r; this.loading = false; },
      error: () => {
        this.loading = false;
        this.reminders = [
          { id: 1, userId: 1, type: 'BIRTHDAY', title: "Alice's Birthday", dueDate: '2024-05-15', completed: false },
          { id: 2, userId: 1, type: 'APPOINTMENT', title: 'Coffee with Bob', dueDate: '2024-04-20', completed: false }
        ];
      }
    });
  }

  saveReminder(): void {
    if (this.reminderForm.invalid) return;
    this.reminderService.createReminder(this.reminderForm.value).subscribe({
      next: () => {
        this.snackBar.open('Reminder created!', 'Close', { duration: 2000 });
        this.showForm = false;
        this.reminderForm.reset({ type: 'CUSTOM' });
        this.loadReminders();
      },
      error: () => this.snackBar.open('Error creating reminder', 'Close', { duration: 3000 })
    });
  }

  deleteReminder(id: number): void {
    this.reminderService.deleteReminder(id).subscribe({
      next: () => { this.snackBar.open('Reminder deleted', 'Close', { duration: 2000 }); this.loadReminders(); },
      error: () => this.snackBar.open('Error deleting reminder', 'Close', { duration: 3000 })
    });
  }

  getReminderIcon(type: ReminderType): string {
    const icons: Record<ReminderType, string> = { BIRTHDAY: 'cake', APPOINTMENT: 'event', CUSTOM: 'notifications' };
    return icons[type];
  }
}
