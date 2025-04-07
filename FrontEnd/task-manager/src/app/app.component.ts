import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UserDetailsService } from './features/tasks/services/user-details.service';

@Component({
	selector: 'app-root',
	imports: [RouterOutlet],
	templateUrl: './app.component.html',
	styleUrl: './app.component.scss'
})

export class AppComponent implements OnInit {
	constructor(private readonly userDetailsService: UserDetailsService) { }

	ngOnInit(): void {
		// Cargar los datos del usuario desde localStorage (si existen)
		this.userDetailsService.loadUserFromStorage();
	}
}
