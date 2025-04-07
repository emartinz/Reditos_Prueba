import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import {
	BehaviorSubject,
	catchError,
	filter,
	switchMap,
	take,
	throwError,
} from 'rxjs';
import { AuthUtils } from '../../utils/auth.utils';
import { constants } from '../../config/constants';
import { AuthService } from '../services/auth.service';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

/**
 * Interceptor HTTP funcional para adjuntar token JWT y manejar errores 401.
 *
 * - Adjunta automáticamente el token JWT en todas las solicitudes salientes (excepto las excluidas).
 * - Si la respuesta es 401, intenta refrescar el token con el `refreshToken`.
 * - Si el refresco es exitoso, reintenta la solicitud original.
 */
export const customInterceptor: HttpInterceptorFn = (req, next) => {
	const authService = inject(AuthService);
	const token = AuthUtils.getTokenFromLocalStorage();

	// Lista de endpoints que NO deben incluir token ni ser interceptados (login, registro, refresh).
	const excludedUrls = [
		constants.auth.login,
		constants.auth.register,
		constants.auth.refreshToken,
	];

	// Si la URL está en la lista de exclusión, no se modifica la solicitud.
	if (excludedUrls.some((url) => req.url.startsWith(url))) {
		console.log('interceptor: ' + req.url + ' excluida.');
		return next(req);
	}

	// Si hay token, clonamos la solicitud original y agregamos el encabezado Authorization.
	const authReq = token
		? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
		: req;

	// Enviamos la solicitud y atrapamos errores.
	return next(authReq).pipe(
		catchError((error: any) => {
			// Si obtenemos un error 401, intentamos refrescar el token.
			if (
				error instanceof HttpErrorResponse &&
				error.status === 401 &&
				!isRefreshing
			) {
				isRefreshing = true;
				refreshTokenSubject.next(null);

				return authService.refreshToken().pipe(
					switchMap((response: any) => {
						isRefreshing = false;

						const newToken = response.data.accessToken;
						refreshTokenSubject.next(newToken);

						// Clonamos y reenviamos la solicitud original con el nuevo token.
						const updatedReq = req.clone({
							setHeaders: { Authorization: `Bearer ${newToken}` },
						});

						return next(updatedReq);
					}),
					catchError((err) => {
						// Si falla el refresh, cerramos sesión y propagamos el error.
						isRefreshing = false;
						authService.logout();
						return throwError(() => err);
					})
				);
			}

			// Si ya se está refrescando el token, esperamos hasta que esté disponible para reenviar la solicitud.
			if (error instanceof HttpErrorResponse && error.status === 401) {
				return refreshTokenSubject.pipe(
					filter((token) => token !== null),
					take(1),
					switchMap((token) => {
						const retryReq = req.clone({
							setHeaders: { Authorization: `Bearer ${token}` },
						});
						return next(retryReq);
					})
				);
			}

			// Para cualquier otro error, simplemente lo propagamos.
			return throwError(() => error);
		})
	);
};
