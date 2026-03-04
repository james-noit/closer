import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { LoginComponent } from './components/auth/login/login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ContactsListComponent } from './components/contacts/contacts-list/contacts-list.component';
import { ContactDetailComponent } from './components/contacts/contact-detail/contact-detail.component';
import { ConversationsListComponent } from './components/messages/conversations-list/conversations-list.component';
import { ConversationComponent } from './components/messages/conversation/conversation.component';
import { RemindersComponent } from './components/reminders/reminders.component';
import { SettingsComponent } from './components/settings/settings.component';
import { InviteComponent } from './components/invite/invite.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'contacts', component: ContactsListComponent, canActivate: [AuthGuard] },
  { path: 'contacts/:id', component: ContactDetailComponent, canActivate: [AuthGuard] },
  { path: 'messages', component: ConversationsListComponent, canActivate: [AuthGuard] },
  { path: 'messages/:contactId', component: ConversationComponent, canActivate: [AuthGuard] },
  { path: 'reminders', component: RemindersComponent, canActivate: [AuthGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [AuthGuard] },
  { path: 'invite', component: InviteComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
