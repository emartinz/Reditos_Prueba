import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { ApiResponse } from '../../models/general/ApiResponse';
import { User } from '../../models/entity/User';
import { constants } from '../../config/constants';
import { Router } from '@angular/router';
import { AuthUtils } from '../../utils/auth.utils';
import { jwtDecode } from 'jwt-decode';
import { UserDetailsService } from '../../features/tasks/services/user-details.service';

@Injectable({
	providedIn: 'root',
})
export class AuthService {
	private readonly baseUrl = constants.auth.baseUrl;
	private refreshTokenTimeout?: any;

	constructor(
		private readonly http: HttpClient,
		private readonly router: Router,
		private readonly userDetailsService: UserDetailsService
	) {
		this.startRefreshTokenTimer();
	}

	login(username: string, password: string): Observable<any> {
		return this.http.post<ApiResponse<any>>(
			constants.auth.login, { username, password }
		).pipe(
			tap(response => {
				if (response.status === 'SUCCESS') {
					this.initSession(response.data);
				}
			})
		);
	}

	register(
		username: string,
		password: string,
		roles: any[]
	): Observable<any> {
		const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

		return this.http.post<ApiResponse<User>>(
			constants.auth.register,
			{ username, password, roles },
			{ headers }
		);
	}

	refreshToken(): Observable<any> {
		const refreshToken = localStorage.getItem('refreshToken');
		if (!refreshToken) {
			return throwError(() => new Error('No refresh token available'));
		}

		return this.http.post<ApiResponse<any>>(
			constants.auth.refreshToken,
			{ refreshToken }
		).pipe(
			tap(response => {
				if (response.status === 'SUCCESS') {
					this.initSession(response.data);
				}
			}),
			catchError(error => {
				this.logout();
				return throwError(() => error);
			})
		);
	}

	private initSession(data: any): void {
		if (data.accessToken) {
			localStorage.setItem('jwt', data.accessToken);
			localStorage.setItem('refreshToken', data.refreshToken);

			const decodedToken: any = jwtDecode(data.accessToken);
			const userId = decodedToken.userId;

			this.userDetailsService.getUserInfo(userId).subscribe({
				next: (response) => {
					const user = response?.data;
					if (user?.username) {
						localStorage.setItem('username', user.username);
					}

					// Extraer primer nombre y primer apellido
					const firstName = user?.firstName?.split(' ')[0] ?? '';
					const lastName = user?.lastName?.split(' ')[0] ?? '';
					const displayName = `${firstName} ${lastName}`.trim();

					if (displayName && user?.username) {
						this.userDetailsService.setUser(displayName, user.username);
					}
				},
				error: (err) => {
					console.error('Error al obtener datos del usuario:', err);
				}
			});

			this.startRefreshTokenTimer();
		}
	}

	private startRefreshTokenTimer(): void {
		// Limpiar el timer existente si hay uno
		if (this.refreshTokenTimeout) {
			clearTimeout(this.refreshTokenTimeout);
		}

		const token = AuthUtils.getTokenFromLocalStorage();
		if (!token) return;

		try {
			const decodedToken = AuthUtils.decodeToken(token);
			// Calcular el tiempo hasta que el token expire (menos 1 minuto para dar margen)
			const expires = new Date(decodedToken.exp * 1000);
			const timeout = expires.getTime() - Date.now() - (60 * 1000);

			if (timeout > 0) {
				this.refreshTokenTimeout = setTimeout(() => {
					this.refreshToken().subscribe();
				}, timeout);
			}
		} catch (error) {
			console.error('Error starting refresh token timer:', error);
		}
	}

	logout(): void {
		// Limpiar el timer de refresh
		if (this.refreshTokenTimeout) {
			clearTimeout(this.refreshTokenTimeout);
		}

		// Eliminar tokens y datos de usuario
		localStorage.removeItem('jwt');
		localStorage.removeItem('refreshToken');
		localStorage.removeItem('username');
		localStorage.removeItem('displayName');

		// Redirigir al login
		this.router.navigate(['/login']);
	}
}