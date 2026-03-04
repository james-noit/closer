import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-invite',
  templateUrl: './invite.component.html',
  styleUrls: ['./invite.component.scss']
})
export class InviteComponent {
  inviteForm: FormGroup;
  shareUrl = 'https://closer.app/join?ref=user123';

  constructor(private fb: FormBuilder, private snackBar: MatSnackBar) {
    this.inviteForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  sendInvite(): void {
    if (this.inviteForm.invalid) return;
    this.snackBar.open('Invitation sent!', 'Close', { duration: 2000 });
    this.inviteForm.reset();
  }

  shareOnWhatsApp(): void {
    window.open(`https://api.whatsapp.com/send?text=Join me on Closer! ${this.shareUrl}`, '_blank');
  }

  shareOnTelegram(): void {
    window.open(`https://t.me/share/url?url=${this.shareUrl}&text=Join me on Closer!`, '_blank');
  }

  copyLink(): void {
    navigator.clipboard.writeText(this.shareUrl).then(() => {
      this.snackBar.open('Link copied!', 'Close', { duration: 2000 });
    });
  }
}
