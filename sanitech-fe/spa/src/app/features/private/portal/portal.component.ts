import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { filter, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './portal.component.html'
})
export class PortalComponent implements OnInit, OnDestroy {
  headerTitle = 'Area riservata';
  headerDescription = '';
  private readonly destroy$ = new Subject<void>();

  constructor(private router: Router, private auth: AuthService) {}

  ngOnInit(): void {
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd), takeUntil(this.destroy$)).subscribe(() => {
      this.setHeaderContent();
    });
    if (!this.router.url.endsWith('/portal')) {
      this.setHeaderContent();
      return;
    }
    if (this.auth.hasRole('ROLE_ADMIN')) {
      this.router.navigate(['/portal/admin']);
      return;
    }
    if (this.auth.hasRole('ROLE_DOCTOR')) {
      this.router.navigate(['/portal/doctor']);
      return;
    }
    this.router.navigate(['/portal/patient']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setHeaderContent(): void {
    if (this.auth.hasRole('ROLE_ADMIN')) {
      this.headerTitle = 'Area Amministratore';
      this.headerDescription = 'Supervisiona directory, audit, notifiche e televisite.';
      return;
    }
    if (this.auth.hasRole('ROLE_DOCTOR')) {
      this.headerTitle = 'Area Medico';
      this.headerDescription = 'Gestisci agenda clinica, prescrizioni e televisite.';
      return;
    }
    this.headerTitle = 'Area Paziente';
    this.headerDescription = 'Gestisci appuntamenti, consensi, documenti e pagamenti.';
  }
}
