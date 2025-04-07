import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../core/components/navbar/navbar.component';

@Component({
	selector: 'app-main-layout',
	standalone: true,
	imports: [CommonModule, RouterOutlet, NavbarComponent],
	templateUrl: './main-layout.component.html',
})
export class MainLayoutComponent { }