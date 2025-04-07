import { jwtDecode } from 'jwt-decode';

export class AuthUtils {

	/**
	 * Verifica si el token JWT ha expirado.
	 *
	 * @param token El token JWT que se desea verificar.
	 * @returns Devuelve `true` si el token ha expirado, de lo contrario `false`.
	 */
	static isTokenExpired(token: string): boolean {
		if (!token) return true;

		try {
			const decodedToken = this.decodeToken(token);
			const expirationDate = new Date(decodedToken.exp * 1000);  // Convertir el tiempo de expiración de Unix a Date
			return expirationDate < new Date();
		} catch (error) {
			console.error('Error al decodificar el token:', error);
			return true;  // En caso de error en la decodificación, considerar el token como expirado
		}
	}

	/**
	 * Obtiene el token JWT desde el almacenamiento local.
	 *
	 * @returns Devuelve el token JWT almacenado en `localStorage`, o `null` si no existe.
	 */
	static getTokenFromLocalStorage(): string | null {
		return localStorage.getItem('jwt');
	}

	/**
	 * Obtiene el payload (datos) del token JWT almacenado en el almacenamiento local.
	 *
	 * @returns El payload decodificado del token, o `null` si no se puede obtener o decodificar el token.
	 */
	static getPayloadFromToken(): any {
		const token = this.getTokenFromLocalStorage();
		if (!token) return null;

		try {
			const decodedToken = this.decodeToken(token);
			return decodedToken;
		} catch (error) {
			console.error('Error al decodificar el token:', error);
			return null;
		}
	}

	/**
	 * Decodifica el token JWT sin verificar su firma.
	 *
	 * @param token El token JWT que se desea decodificar.
	 * @returns El payload del token como objeto JavaScript.
	 * @throws Error si el token no es válido (no tiene 3 partes).
	 */
	static decodeToken(token: string): any {
		const parts = token.split('.');
		if (parts.length !== 3) {
			throw new Error('Token JWT no válido');
		}
		const decoded = atob(parts[1]); // Decodificar la parte payload del token
		return JSON.parse(decoded); // Convertir el payload a un objeto JavaScript
	}

	/**
	 * Verifica si el usuario tiene un rol específico.
	 *
	 * @param role El nombre del rol que se desea verificar.
	 * @returns `true` si el usuario tiene el rol, `false` si no lo tiene o si no existe el payload de roles.
	 */
	static hasRole(role: string): boolean {
		const payload = this.getPayloadFromToken();
		if (!payload?.roles) return false;
		return payload.roles.includes(role);  // Verifica si el rol está en el array de roles
	}

	/**
	 * Obtiene los roles del usuario desde el token JWT almacenado.
	 *
	 * @returns Un array de roles del usuario. Si no hay roles o el token es inválido, devuelve un array vacío.
	 */
	static getUserRoles(): string[] {
		const token = this.getTokenFromLocalStorage();
		if (!token) return [];

		const payload = JSON.parse(atob(token.split('.')[1])); // Decodificar el JWT
		console.log(payload);
		return Array.isArray(payload.roles) ? payload.roles : [payload.roles]; // Garantiza que siempre sea un array
	}

	/**
	 * Verifica si el usuario está autenticado basándose en la existencia y validez del token JWT.
	 *
	 * @returns `true` si el token existe y no ha expirado, de lo contrario `false`.
	 */
	static isAuthenticated(): boolean {
		const token = this.getTokenFromLocalStorage();
		return !!token && !this.isTokenExpired(token);
	}

	/**
	 * Cierra la sesión eliminando el token y el nombre de usuario del almacenamiento local.
	 */
	static closeSession(): void {
		// Elimina el token JWT y el nombre de usuario del almacenamiento local
		localStorage.removeItem('jwt');
		localStorage.removeItem('username');
		localStorage.removeItem('refreshToken');
	}
}