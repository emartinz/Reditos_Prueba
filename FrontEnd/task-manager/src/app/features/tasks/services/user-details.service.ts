import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiResponse } from '../../../models/general/ApiResponse';
import { User } from '../../../models/entity/User';
import { constants } from '../../../config/constants';
import { UserDetailsResponseDTO } from '../../../models/general/UserDetailsResponseDTO';

/**
 * Servicio encargado de gestionar la información extendida del usuario.
 * Proporciona métodos para registrar, actualizar y obtener datos de usuario,
 * así como exponer nombre de usuario y nombre completo como observables.
 */
@Injectable({
	providedIn: 'root'
})
export class UserDetailsService {
	private readonly baseUrl = constants.task.user;

	// Subjects internos para el estado del usuario
	private readonly displayNameSubject = new BehaviorSubject<string>('');
	private readonly usernameSubject = new BehaviorSubject<string>('');

	/**
	 * Observable del nombre completo del usuario (firstName + lastName)
	 */
	displayName$ = this.displayNameSubject.asObservable();

	/**
	 * Observable del username del usuario
	 */
	username$ = this.usernameSubject.asObservable();

	constructor(private readonly http: HttpClient) { }

	/**
	 * Registra o actualiza un usuario en el backend.
	 *
	 * @param userDetails - Datos del usuario a registrar o actualizar
	 * @returns Observable con la respuesta del backend
	 */
	registerOrUpdateUser(userDetails: User): Observable<ApiResponse<User>> {
		const body = {
			userId: userDetails.userid,
			username: userDetails.username,
			firstName: userDetails.firstName,
			lastName: userDetails.lastName,
			...(userDetails.email && { email: userDetails.email })
		};

		return this.http.post<ApiResponse<User>>(
			`${this.baseUrl}/registerUser`,
			body
		);
	}

	/**
	 * Obtiene la información del usuario desde el backend a partir de su ID.
	 *
	 * @param id - ID del usuario
	 * @returns Observable con la información del usuario
	 */
	getUserInfo(id: number): Observable<ApiResponse<UserDetailsResponseDTO>> {
		return this.http.get<ApiResponse<UserDetailsResponseDTO>>(
			`${this.baseUrl}/${id}`
		);
	}

	/**
	 * Limpia los datos del usuario almacenados en memoria.
	 * Se utiliza típicamente al cerrar sesión.
	 */
	clearUser(): void {
		this.displayNameSubject.next('');
		this.usernameSubject.next('');
	}

	/**
	 * Establece los datos del usuario en memoria.
	 * Esto permite que otros componentes accedan a estos datos sin consultar al backend.
	 *
	 * @param displayName - Nombre completo del usuario
	 * @param username - Nombre de usuario
	 */
	setUser(displayName: string, username: string): void {
		this.displayNameSubject.next(displayName);
		this.usernameSubject.next(username);
		localStorage.setItem('displayName', displayName);
		localStorage.setItem('username', username);
	}

	/**
	 * Carga los datos del usuario desde localStorage si existen.
	 * Se debe llamar al inicializar la aplicación.
	 */
	loadUserFromStorage(): void {
		const displayName = localStorage.getItem('displayName');
		const username = localStorage.getItem('username');
		if (displayName) this.displayNameSubject.next(displayName);
		if (username) this.usernameSubject.next(username);
	}

}