export interface Role {
    id: number;
    name: string;
}

export interface User {
    id?: number;
    userid: number;
    username: string;
    password?: string;
    email?: string;
    firstName: string;
    lastName: string;
    roles?: { id: number }[];
}