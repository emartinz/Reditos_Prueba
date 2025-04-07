import { Component, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { UserDetailsService } from '../../../features/tasks/services/user-details.service';
import { User } from '../../../models/entity/User';
import { AuthService } from '../../services/auth.service';

@Injectable()
@Component({
	selector: 'app-login',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule
	],
	templateUrl: './login.component.html',
	styleUrl: './login.component.scss'
})
export class LoginComponent {
	username: string = '';
	password: string = '';
	newUsername: string = '';
	firstName: string = '';
	lastName: string = '';
	email: string = '';
	newPassword: string = '';
	confirmPassword: string = '';
	errorMessage: string = '';
	isRegister: boolean = false;


	constructor(
		private readonly authService: AuthService,
		private readonly userDetailsService: UserDetailsService,
		private readonly router: Router
	) {

	}

	toggleRegister() {
		this.isRegister = !this.isRegister;
	}

	login(redirect: boolean = true): Promise<void> {
		return new Promise((resolve, reject) => {
			this.authService.login(this.username, this.password).subscribe({
				next: (response: any) => {
					if (response.status === 'SUCCESS') {
						if (redirect) {
							this.router.navigate(['/tasks']);
						}
						resolve();
					} else {
						this.errorMessage = 'Error en el inicio de sesión';
						reject(new Error('Login failed'));
					}
				},
				error: (error) => {
					this.errorMessage = 'Error en el inicio de sesión';
					console.error('Login error:', error);
					reject(new Error(error));
				}
			});
		});
	}

	async register() {
		// Validar si las contraseñas coinciden
		if (this.newPassword !== this.confirmPassword) {
			this.errorMessage = 'Las contraseñas no coinciden.';
			return;
		}

		const roles = [{ id: 1 }]; // Rol de Usuario

		try {
			const response = await firstValueFrom(
				this.authService.register(this.newUsername, this.newPassword, roles)
			);

			if (response.status === 'SUCCESS' && response.data) {
				console.log('Registro en auth exitoso:', response);
				const userId = response.data.id;
				this.username = response.data.username;

				// Iniciar sesión
				this.password = this.newPassword;
				await this.login(false);

				const newUser: User = {
					userid: userId,
					username: this.newUsername,
					firstName: this.firstName,
					lastName: this.lastName,
					email: this.email,
					roles: roles
				};

				console.log(newUser);

				const detailsResponse = await firstValueFrom(
					this.userDetailsService.registerOrUpdateUser(newUser)
				);

				console.log('Detalles de usuario guardados:', detailsResponse);
				this.router.navigate(['/tasks']);
			}
		} catch (error) {
			this.errorMessage = 'Error en el registro';
			console.error('Error en el registro:', error);
		}
	}
}
