import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../models/general/ApiResponse';
import { User } from '../../models/entity/User';
import { constants } from '../../config/constants';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly baseUrl = constants.urlAuth + '/api';
  private token: string | null = null;

  constructor(private readonly http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    return this.http.post<ApiResponse<string>>(
      `${this.baseUrl}/login`, 
      { username, password }
    );
  }

  register(
    username: string, 
    password: string, 
    roles: any[]
  ): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
  
    return this.http.post<ApiResponse<User>>(
      `${this.baseUrl}/register`,
      { username, password, roles },
      { headers }
    );
  }

  setToken(token: string): void {
    this.token = token;
  }

  getToken(): string | null {
    return this.token;
  }

  logout(): void {
    this.token = null;
  }
}