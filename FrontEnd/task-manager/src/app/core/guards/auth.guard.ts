import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
	const router = inject(Router);
	const token = localStorage.getItem('jwt');

	if (token && isTokenValid(token)) {
		return true;
	}

	router.navigate(['/login']);
	return false;
};

/**
 * Verifica si el token JWT está vigente
 */
function isTokenValid(token: string): boolean {
	try {
		const payload = JSON.parse(atob(token.split('.')[1]));
		const expiration = payload.exp;
		const now = Math.floor(Date.now() / 1000);
		return expiration > now;
	} catch (e) {
		return false;
	}
}