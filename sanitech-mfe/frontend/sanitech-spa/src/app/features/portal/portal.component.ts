import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-portal',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './portal.component.html'
})
export class PortalComponent implements OnInit {
  constructor(private router: Router, private auth: AuthService) {}

  ngOnInit(): void {
    if (!this.router.url.endsWith('/portal')) {
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
}
