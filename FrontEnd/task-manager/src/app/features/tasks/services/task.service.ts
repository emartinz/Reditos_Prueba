import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../models/general/ApiResponse';
import { Task } from '../models/entities/Task';
import { constants } from '../../../config/constants';

/**
 * Servicio para gestionar operaciones relacionadas con tareas.
 * Proporciona métodos CRUD, filtrado y consultas paginadas.
 */
@Injectable({
	providedIn: 'root',
})
export class TaskService {

	constructor(private readonly http: HttpClient) { }

	/**
	 * Obtiene todas las tareas disponibles.
	 * @returns Observable con la respuesta de tipo ApiResponse<Task[]>
	 */
	getTasks(): Observable<ApiResponse<Task[]>> {
		return this.http.get<ApiResponse<Task[]>>(constants.task.tasks);
	}

	/**
	 * Obtiene las tareas del usuario autenticado (paginado).
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @returns Observable con la respuesta de tipo ApiResponse<Task[]>
	 */
	getUserTasks(page: number, size: number): Observable<ApiResponse<Task[]>> {
		let url = `${constants.task.tasks}/getAll`;
		const params: string[] = [];

		if (page !== undefined) params.push(`page=${page}`);
		if (size !== undefined) params.push(`size=${size}`);
		if (params.length > 0) url += `?${params.join('&')}`;

		return this.http.get<ApiResponse<Task[]>>(url);
	}

	/**
	 * Obtiene todas las tareas del sistema (sólo para administradores).
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @returns Observable con la respuesta de tipo ApiResponse<Task[]>
	 */
	getAllTasks(page: number, size: number): Observable<ApiResponse<Task[]>> {
		let url = `${constants.task.tasks}/admin/getAll`;
		const params: string[] = [];

		if (page !== undefined) params.push(`page=${page}`);
		if (size !== undefined) params.push(`size=${size}`);
		if (params.length > 0) url += `?${params.join('&')}`;

		return this.http.get<ApiResponse<Task[]>>(url);
	}

	/**
	 * Obtiene una tarea por su ID.
	 * @param id Identificador de la tarea
	 * @returns Observable con la respuesta de tipo ApiResponse<Task>
	 */
	getTaskById(id: number): Observable<ApiResponse<Task>> {
		return this.http.get<ApiResponse<Task>>(`${constants.task.tasks}/${id}`);
	}

	/**
	 * Crea una nueva tarea.
	 * @param task Objeto de tipo Task a crear
	 * @returns Observable con la respuesta de tipo ApiResponse<Task>
	 */
	createTask(task: Task): Observable<ApiResponse<Task>> {
		return this.http.post<ApiResponse<Task>>(constants.task.tasks, task);
	}

	/**
	 * Actualiza una tarea existente.
	 * @param task Objeto de tipo Task con datos actualizados
	 * @returns Observable con la respuesta de tipo ApiResponse<Task>
	 */
	updateTask(task: Task): Observable<ApiResponse<Task>> {
		return this.http.put<ApiResponse<Task>>(`${constants.task.tasks}/${task.id}`, task);
	}

	/**
	 * Elimina una tarea por su ID.
	 * @param id Identificador de la tarea
	 * @returns Observable con la respuesta de tipo ApiResponse<void>
	 */
	deleteTask(id: number): Observable<ApiResponse<void>> {
		return this.http.delete<ApiResponse<void>>(`${constants.task.tasks}/${id}`);
	}

	/**
	 * Filtra tareas según los criterios especificados.
	 * @param title Título de la tarea
	 * @param status Estado de la tarea (ej. 'PENDING', 'DONE')
	 * @param priority Prioridad de la tarea (ej. 'HIGH', 'LOW')
	 * @param page Número de página
	 * @param size Tamaño de página
	 * @param admin Si es true, usa el endpoint de administración
	 * @returns Observable con la respuesta de tipo ApiResponse<Task[]>
	 */
	filterTasks(
		title: string,
		status: string,
		priority: string,
		page: number,
		size: number,
		admin: boolean = false
	): Observable<ApiResponse<Task[]>> {
		const queryParams = new URLSearchParams();

		if (title) queryParams.append('title', title);
		if (status) queryParams.append('status', status);
		if (priority) queryParams.append('priority', priority);

		queryParams.append('page', page.toString());
		queryParams.append('size', size.toString());

		const basePath = 'filter';
		const url = `${constants.task.tasks}/${basePath}?${queryParams.toString()}`;

		return this.http.get<ApiResponse<Task[]>>(url);
	}

	/**
	 * Obtiene los roles del usuario actual decodificando el token JWT.
	 * @returns Arreglo de roles como string[]
	 */
	getUserRoles(): string[] {
		const token = localStorage.getItem('jwt');
		if (!token) return [];

		try {
			const payload = JSON.parse(atob(token.split('.')[1]));
			return Array.isArray(payload.roles) ? payload.roles : [payload.roles];
		} catch (e) {
			console.error('Error decoding JWT:', e);
			return [];
		}
	}
}