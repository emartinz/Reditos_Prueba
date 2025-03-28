import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { jwtDecode } from 'jwt-decode';


@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  newUsername: string = '';
  email: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  errorMessage: string = '';
  isRegister: boolean = false;
  

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    
  }

  login() {
    this.authService.login(this.username, this.password).subscribe({
      next: (response: any) => {
        if (response.status === 'SUCCESS') {
          let token = response.data;
          this.authService.setToken(token);
          const decodedToken: any = jwtDecode(token);
          console.log(decodedToken);
          this.username = decodedToken.sub;
          localStorage.setItem('jwt', token);
          localStorage.setItem('username', this.username);
          this.router.navigate(['/tasks']);
        }
      },
      error: (error) => {
        this.errorMessage = 'Login failed';
        console.error('Login error:', error);
      }
    });
  }

  register() {
    // Lógica para el registro
  }

  toggleRegister() {
    this.isRegister = !this.isRegister;
  }
}
