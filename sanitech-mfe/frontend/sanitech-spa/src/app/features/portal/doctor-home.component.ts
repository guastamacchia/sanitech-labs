import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

type UpcomingVisit = {
  department: string;
  patient: string;
  date: string;
  time: string;
  modality: string;
  reason: string;
};

@Component({
  selector: 'app-doctor-home',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './doctor-home.component.html'
})
export class DoctorHomeComponent {
  upcomingVisits: UpcomingVisit[] = [
    {
      department: 'Cardiologia',
      patient: 'Anna Conti',
      date: '12/05/2024',
      time: '09:30',
      modality: 'In presenza',
      reason: 'Controllo cardiologico'
    },
    {
      department: 'Neurologia',
      patient: 'Luca Rinaldi',
      date: '13/05/2024',
      time: '11:00',
      modality: 'Da remoto',
      reason: 'Visita di follow-up'
    },
    {
      department: 'Oncologia',
      patient: 'Elena Greco',
      date: '14/05/2024',
      time: '15:00',
      modality: 'In presenza',
      reason: 'Monitoraggio terapia'
    }
  ];

  pageSizeOptions = [5, 10];
  pageSize = 5;
  currentPage = 1;

  get paginatedVisits(): UpcomingVisit[] {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.upcomingVisits.slice(start, end);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.upcomingVisits.length / this.pageSize));
  }

  changePage(nextPage: number): void {
    this.currentPage = Math.min(Math.max(nextPage, 1), this.totalPages);
  }

  resetPagination(): void {
    this.currentPage = 1;
  }
}
