import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { UserDetailsService } from '../../../features/tasks/services/user-details.service';

@Component({
	selector: 'app-navbar',
	standalone: true,
	imports: [CommonModule],
	templateUrl: './navbar.component.html',
	styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent {
	displayName$;

	private readonly authService = inject(AuthService);

	constructor(private readonly userDetailsService: UserDetailsService) {
		this.displayName$ = this.userDetailsService.displayName$;
	}

	logout(): void {
		this.authService.logout();
	}
}
