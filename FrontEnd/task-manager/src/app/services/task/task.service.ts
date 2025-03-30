import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/general/ApiResponse';
import { Task } from '../../models/entity/Task';
import { constants } from '../../config/constants';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly baseUrl = constants.urlTask + '/api/tasks';
  
  constructor(private readonly http: HttpClient) {}

  /**
   * EndPoint para listar todas las tareas (Admin)
   * @param page 
   * @param size 
   * @returns 
   */
  
  getUserTasks(page: number, size: number): Observable<any> {
    const token = localStorage.getItem('jwt');
    let headers = new HttpHeaders()
      .set('Authorization', `Bearer ${token}`)  // Encabezado de autorización
      .set('Content-Type', 'application/json')  // Tipo de contenido
      .set('Accept', 'application/json')  // Tipo de respuesta esperada
      .set('Cache-Control', 'no-cache');  // Deshabilitar caché
    
    let url = `${this.baseUrl}/getAll`;
    const params: string[] = [];

    if (page !== undefined) {
      params.push(`page=${page}`);
    }
    if (size !== undefined) {
      params.push(`size=${size}`);
    }
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }

    return this.http.get<ApiResponse<Task[]>>(url, { headers });
  }

  getAllTasks(page: number, size: number): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    let url = `${this.baseUrl}/admin/getAll`;
    const params: string[] = [];

    if (page !== undefined) {
      params.push(`page=${page}`);
    }
    if (size !== undefined) {
      params.push(`size=${size}`);
    }
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }

    return this.http.get<ApiResponse<Task[]>>(url, { headers });
  }

  /**
   * EndPoint para crear una nueva tarea
   * @param task
   * @returns 
   */
  createTask(task: any): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.post<ApiResponse<Task>>(
      `${this.baseUrl}/create`,
      task,
      { headers }
    );
  }

  /**
   * EndPoint para actualizar una tarea
   * @param id 
   * @param task 
   * @returns 
   */
  updateTask(id: number, task: any): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.put<ApiResponse<Task>>(
      `${this.baseUrl}/${id}`,
      task,
      { headers }
    );
  }

  /**
   * EndPoint para borrar una tarea
   * @param id 
   * @returns 
   */
  deleteTask(id: number): Observable<any> {
    const token = localStorage.getItem('jwt');
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`, { headers });
  }


  filterTasks(title: string, status: string, priority: string, page: number, size: number, admin: boolean = false): Observable<any> {
    const queryParams = new URLSearchParams();

    if (title) {
      queryParams.append('title', title);
    }
    if (status) {
      queryParams.append('status', status);
    }
    if (priority) {
      queryParams.append('priority', priority);
    }
    
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());

    const token = localStorage.getItem('jwt');

    let url = `${this.baseUrl}/filter`;

    return this.http.get<ApiResponse<Task[]>>(`${url}?${queryParams.toString()}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
  }

  getUserRoles(): string[] {
    const token = localStorage.getItem('jwt');
    if (!token) return [];

    const payload = JSON.parse(atob(token.split('.')[1])); // Decodificar el JWT
    console.log(payload);
    return Array.isArray(payload.roles) ? payload.roles : [payload.roles]; // Garantiza que siempre sea un array
}
}


