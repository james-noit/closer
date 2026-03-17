import { Routes } from '@angular/router';
import { Home } from './home/home';
import { LoginComponent } from './auth/login.component';
import { AuthGuard } from './auth/auth.guard';
import { AdminGuard } from './auth/admin.guard';

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
			},
			{
				path: 'admin/grupos',
				canActivate: [AdminGuard],
				loadComponent: () => import('./admin/grupos/grupos').then(m => m.AdminGrupos)
			}
		]
	},
	// catch-all: unauthenticated users will be sent to login, others to home
	{
		path: '**',
		redirectTo: ''
	}
];
