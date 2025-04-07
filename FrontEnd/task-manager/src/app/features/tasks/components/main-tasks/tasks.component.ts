import { Component, Injectable, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { Task, TaskPriority, TaskStatus } from '../../models/entities/Task';
import { AuthService } from '../../../../core/services/auth.service';

/**
 * Componente para la gestión de tareas
 * Permite crear, actualizar, eliminar y filtrar tareas
 * Incluye funcionalidades de paginación y búsqueda
 */
@Injectable()
@Component({
	selector: 'app-tasks',
	standalone: true,
	imports: [
		CommonModule,
		FormsModule
	],
	templateUrl: './tasks.component.html',
	styleUrl: './tasks.component.scss'
})
export class TasksComponent implements OnInit {
	// Propiedades de autenticación
	isAdmin: boolean = false;
	username: string = '';
	displayName: string = '';

	// Propiedades de tareas
	tasks: Task[] = [];
	task: Task = {
		id: null,
		title: '',
		description: '',
		status: TaskStatus.PENDIENTE,
		priority: TaskPriority.MEDIA,
		dueDate: null,
		userDetails: { id: 0, name: '', username: '', email: '', firstName: '', lastName: '' },
		createdAt: new Date(),
		updatedAt: new Date()
	};

	// Enumeraciones para el template
	taskStatuses = Object.values(TaskStatus);
	taskPriorities = Object.values(TaskPriority);

	// Propiedades de búsqueda y filtrado
	searchParams = {
		title: '',
		status: '',
		priority: ''
	};
	isFiltered = false;

	// Propiedades de paginación
	defaultItemsPerPage = 5;
	itemsPerPage = this.defaultItemsPerPage;
	currentPage = 0;
	totalPages = 1;
	currentPageInput: number = this.currentPage + 1;
	number = 5;
	firstPage = true
	lastPage = true

	constructor(
		private readonly taskService: TaskService,
		private readonly router: Router,
		private readonly authService: AuthService
	) { }

	/**
	 * Inicializa el componente
	 * Verifica la autenticación y carga las tareas iniciales
	 */
	ngOnInit(): void {
		this.checkToken();
		// Carga valores del localStorage
		this.displayName = localStorage.getItem('displayName') ?? '';
		this.username = localStorage.getItem('username') ?? '';

		const roles: string[] = this.taskService.getUserRoles() || [];
		this.isAdmin = roles.some(role => (role ? role.toLowerCase() === 'admin' : false));

		this.loadTasks();
	}

	/**
	 * Verifica la validez del token JWT
	 * Redirige al login si el token no es válido
	 */
	checkToken(): void {
		const token = localStorage.getItem('jwt');

		if (!token || !this.isTokenValid(token)) {
			this.router.navigate(['/login']);
		}
	}

	/**
	 * Valida la expiración del token JWT
	 * @param token - Token JWT a validar
	 * @returns boolean indicando si el token es válido
	 */
	isTokenValid(token: string): boolean {
		try {
			const payload = JSON.parse(atob(token.split('.')[1]));
			const expiration = payload.exp;
			const now = Math.floor(Date.now() / 1000);
			return expiration > now;
		} catch (error) {
			return false;
		}
	}

	/**
	 * Cierra la sesión del usuario
	 */
	logout(): void {
		this.authService.logout();
	}

	/**
	 * Carga las tareas según el rol del usuario
	 * @param page - Número de página opcional
	 */
	loadTasks(page?: number): void {
		if (page !== undefined) {
			this.currentPage = page;
		}

		const tasksObservable = this.isAdmin
			? this.taskService.getAllTasks(this.currentPage, this.itemsPerPage)
			: this.taskService.getUserTasks(this.currentPage, this.itemsPerPage);

		tasksObservable.subscribe({
			next: (response: any) => {
				if (response.status === 'SUCCESS') {
					this.tasks = response.data.content;
					this.currentPage = response.data.pageable.pageNumber;
					this.totalPages = response.data.totalPages - 1;
					this.firstPage = response.data.first;
					this.lastPage = response.data.last;
				}
			},
			error: (error: any) => {
				console.error('Error al obtener tareas:', error);
			}
		});
	}

	/**
	 * Maneja el envío del formulario de tarea
	 * Crea o actualiza una tarea según corresponda
	 */
	onSubmit(): void {
		if (this.task.id != null) {
			this.taskService.updateTask(this.task).subscribe(() => {
				console.log("update");
				this.loadTasks();
				this.resetTask();
			});
		} else {
			this.taskService.createTask(this.task).subscribe(() => {
				console.log("create");
				this.loadTasks();
				this.resetTask();
			});
		}
	}

	/**
	 * Crea una nueva tarea vacía
	 */
	createTask(): void {
		const newTask: Task = {
			id: null,
			title: '',
			description: '',
			status: TaskStatus.PENDIENTE,
			priority: TaskPriority.MEDIA,
			dueDate: null,
			userDetails: { id: 0, name: '' },
			createdAt: new Date(),
			updatedAt: new Date()
		};
		this.taskService.createTask(newTask).subscribe(() => {
			this.loadTasks();
		});
	}

	/**
	 * Elimina una tarea por su ID
	 * @param id - ID de la tarea a eliminar
	 */
	deleteTask(id: number): void {
		this.taskService.deleteTask(id).subscribe(() => {
			this.loadTasks();
		});
	}

	/**
	 * Actualiza una tarea existente
	 * @param task - Tarea a actualizar
	 */
	updateTask(task: Task): void {
		console.log(task);
		this.task = { ...task };
	}

	/**
	 * Resetea el formulario de tarea a sus valores por defecto
	 */
	resetTask(): void {
		this.task = {
			id: null,
			title: '',
			description: '',
			status: TaskStatus.PENDIENTE,
			priority: TaskPriority.MEDIA,
			dueDate: null,
			userDetails: { id: 0, name: '' },
			createdAt: new Date(),
			updatedAt: new Date()
		};
	}

	/**
	 * Prepara el formulario para crear una nueva tarea
	 */
	preCreateTask(): void {
		this.resetTask();
	}

	/**
	 * Busca tareas según los criterios de filtrado
	 * @param page - Número de página opcional
	 */
	searchTasks(page?: number): void {
		if (page !== undefined) {
			this.currentPage = page;
		}

		this.taskService.filterTasks(
			this.searchParams.title,
			this.searchParams.status,
			this.searchParams.priority,
			this.currentPage,
			this.itemsPerPage,
			this.isAdmin
		).subscribe((response: any) => {
			if (response.status === 'SUCCESS' && response.data) {
				this.tasks = response.data.content;
				this.totalPages = response.data.totalPages - 1;
				this.currentPage = response.data.pageable.pageNumber;
				this.firstPage = response.data.first;
				this.lastPage = response.data.last;
				this.isFiltered = true;
			}
		});
	}

	/**
	 * Limpia los criterios de búsqueda y restablece la vista
	 */
	clearSearch(): void {
		this.searchParams = {
			title: '',
			status: '',
			priority: ''
		};
		this.itemsPerPage = this.defaultItemsPerPage;
		this.loadTasks(0);
		this.isFiltered = false;
	}

	/**
	 * Navega a la siguiente página de resultados
	 */
	nextPage(): void {
		if (this.currentPage + 1 <= this.totalPages) {
			this.currentPage++;
			this.isFiltered ? this.searchTasks() : this.loadTasks();
		}
	}

	/**
	 * Navega a la página anterior de resultados
	 */
	prevPage(): void {
		if (this.currentPage > 0) {
			this.currentPage--;
			this.isFiltered ? this.searchTasks() : this.loadTasks();
		}
	}


	// Método para obtener los números de las páginas
	getPageNumbers(): number[] {
		const maxPagesToShow = 5; // Número máximo de páginas a mostrar
		const pageNumbers: number[] = [];

		const startPage = Math.max(0, this.currentPage - Math.floor(maxPagesToShow / 2));
		const endPage = Math.min(this.totalPages, startPage + maxPagesToShow - 1);

		for (let i = startPage; i <= endPage; i++) {
			pageNumbers.push(i);
		}

		return pageNumbers;
	}

	/**
	 * Navega a una página específica
	 * @param page - Número de página a la que navegar
	 */
	changePage(page: number): void {
		if (page >= 0 && page <= this.totalPages) {
			this.currentPage = page;
			this.loadTasks();
		}
	}
}