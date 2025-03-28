export enum TaskStatus {
    PENDIENTE = 'PENDIENTE',
    EN_PROGRESO = 'EN_PROGRESO',
    COMPLETADA = 'COMPLETADA',
}

export enum TaskPriority {
    ALTA = 'ALTA',
    MEDIA = 'MEDIA',
    BAJA = 'BAJA',
}

export interface UserDetails {
    id: number;
    name: string;
}

export interface Task {
    id?: number | null;
    title: string;
    description: string;
    status: TaskStatus;
    priority: TaskPriority;
    dueDate?: Date | null;
    userDetails: UserDetails;
    createdAt: Date;
    updatedAt: Date;
}