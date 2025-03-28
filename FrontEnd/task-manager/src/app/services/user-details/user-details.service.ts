import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/general/ApiResponse';
import { User } from '../../models/entity/User';


@Injectable({
    providedIn: 'root'
})
export class UserDetailsService {
    private readonly baseURl = 'http://localhost:8081/api/user';

    constructor(private readonly http: HttpClient) {}

    registerOrUpdateUser(userDetails: User): Observable<ApiResponse<User>> {
        // Obtiene el token del localStorage
        const token = localStorage.getItem('jwt'); 
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            Authorization: token ? `Bearer ${token}` : '' // Añade el token si existe
        });

        const body = {
            userId: userDetails.userid,
            username: userDetails.username,
            firstName: userDetails.firstName,
            lastName: userDetails.lastName,
            ...(userDetails.email && { email: userDetails.email })
        };

        return this.http.post<ApiResponse<User>>(
            `${this.baseURl}/registerUser`,
            body,
            { headers }
        );
    }
}