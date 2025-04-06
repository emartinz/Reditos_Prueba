import { Routes } from '@angular/router';
import { TasksComponent } from './features/tasks/components/main-tasks/tasks.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './core/components/login/login.component';

export const routes: Routes = [
	{
		path: '',
		component: MainLayoutComponent,
		children: [
			{ path: '', redirectTo: 'tasks', pathMatch: 'full' },
			{ path: 'tasks', component: TasksComponent, canActivate: [authGuard] }
			// otras rutas privadas...
		]
	},
	{
		path: '',
		component: AuthLayoutComponent,
		children: [
			{ path: 'login', component: LoginComponent }
		]
	},
	{ path: '**', redirectTo: '' }
];
