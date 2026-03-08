import { Routes } from '@angular/router';
import { Home } from './home/home';
import { LoginComponent } from './auth/login.component';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
	{
		path: 'login',
		component: LoginComponent
	},
	{
		path: '',
		canActivate: [AuthGuard],
		children: [
			{
				path: '',
				component: Home
			}
		]
	},
	// catch-all: unauthenticated users will be sent to login, others to home
	{
		path: '**',
		redirectTo: ''
	}
];
