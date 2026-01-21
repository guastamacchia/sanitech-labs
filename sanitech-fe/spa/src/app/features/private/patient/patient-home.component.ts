import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-patient-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './patient-home.component.html'
})
export class PatientHomeComponent {}
