import { Component, OnInit } from '@angular/core';
import { TaskService } from '../../../services/task/task.service';
import { Task, TaskPriority, TaskStatus } from '../../../models/entity/Task';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

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
  tasks: Task[] = [];
  taskStatuses = Object.values(TaskStatus);
  taskPriorities = Object.values(TaskPriority);
  task: Task = { 
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

  searchParams = {
    title: '',
    status: '',
    priority: ''
  };
  defaultItemsPerPage = 5;
  
  username: string | null = '';
  number = 5;
  currentPage = 0;
  totalPages = 1;
  itemsPerPage = 3;

  constructor(private readonly taskService: TaskService, private readonly router: Router, private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.checkToken();
    this.username = localStorage.getItem('username');
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.getUserTasks(this.currentPage, this.itemsPerPage).subscribe({
      next: (response: any) => {
        if (response.status === 'SUCCESS') {
          this.tasks = response.data.content;
          this.currentPage = response.data.pageable.pageNumber;
          this.totalPages = response.data.totalPages - 1;
        }
      },
      error: (error) => {
        console.error('Login error:', error);
      }
    });
  }
  
  onSubmit(): void {
    if (this.task.id != null) {
      // Actualiza la tarea si tiene un id
      this.taskService.updateTask(this.task.id, this.task).subscribe(() => {
        console.log("update");
        this.loadTasks(); // Recarga las tareas después de actualizar
        this.resetTask(); // Resetea la tarea después de la edición
      });
    } else {
      // Crea una nueva tarea si no tiene id
      this.taskService.createTask(this.task).subscribe(() => {
        console.log("create");
        this.loadTasks(); // Recarga las tareas después de crear
        this.resetTask(); // Resetea la tarea después de la creación
      });
    }
  }

  preCreateTask() {
    this.resetTask();
  }

  createTask() {
    const newTask = {
      title: '',
      description: '',
      status: 'PENDIENTE',
      priority: 'MEDIA',
    };
    this.taskService.createTask(newTask).subscribe(() => {
      this.loadTasks();
    });
  }

  deleteTask(id: number) {
    this.taskService.deleteTask(id).subscribe(() => {
      this.loadTasks();
    });
  }

  updateTask(task: Task): void {
    console.log(task);
    this.task = { ...task };
  }

    
  // Resetea los valores de la tarea actual después de crear o editar
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

  changePage(page: number): void {
    if (page >= 0 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadTasks();
    }
  }

  clearSearch() {
    this.searchParams = {
        title: '',
        status: '',
        priority: ''
    };
    this.itemsPerPage = this.defaultItemsPerPage;
    this.loadTasks();
}

  checkToken(): void {
    const token = localStorage.getItem('jwt'); // Obtener el token desde el localStorage

    if (!token || !this.isTokenValid(token)) {
      // Si no hay token o el token no es válido, redirigir al login
      this.router.navigate(['/login']);
    }
  }

  // Función para verificar si el token es válido
  isTokenValid(token: string): boolean {
    try {
      // Decodificar el JWT
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiration = payload.exp;

      // Obtener la fecha actual en segundos
      const now = Math.floor(Date.now() / 1000); 

      // Verificar si el token ha expirado
      return expiration > now;
    } catch (error) {
      // Si hay un error al decodificar el token, considerarlo no válido
      return false;
    }
  }

  // Método para cerrar sesión
  logout(): void {
    // Elimina el token JWT del almacenamiento local (o sesión)
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    
    // Redirige al login
    this.router.navigate(['/login']);
  }

  searchTasks() {
    const queryParams = new URLSearchParams();
  
    if (this.searchParams.title) {
      queryParams.append('title', this.searchParams.title);
    }
    if (this.searchParams.status) {
      queryParams.append('status', this.searchParams.status);
    }
    if (this.searchParams.priority) {
      queryParams.append('priority', this.searchParams.priority);
    }
  
    queryParams.append('page', this.currentPage.toString());
    queryParams.append('size', this.itemsPerPage.toString());
    const token = localStorage.getItem('jwt');
    
    this.http.get(`http://localhost:8081/api/tasks/filter?${queryParams.toString()}`, {
      headers: { Authorization: `Bearer ${token}` }
    }).subscribe((response: any) => {
      if (response.status === 'SUCCESS' && response.data) {
        this.tasks = response.data.content;
        this.totalPages = response.data.totalPages;
        this.currentPage = response.data.pageable.pageNumber;
      }
    });
  }
}