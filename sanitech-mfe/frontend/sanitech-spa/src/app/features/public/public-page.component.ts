import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './public-page.component.html'
})
export class PublicPageComponent {}
