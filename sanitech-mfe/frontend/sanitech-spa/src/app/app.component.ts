import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf, NgFor, AsyncPipe, FormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  mockRole = this.auth.mockRole;

  constructor(public auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.loadDiscovery();
    if (this.auth.isMockEnabled) {
      this.mockRole = this.auth.mockRole;
    }
  }

  login(): void {
    this.auth.login();
  }

  logout(): void {
    this.auth.logout();
  }

  updateMockRole(role: string): void {
    this.auth.selectMockProfile(role);
    this.mockRole = this.auth.mockRole;
  }
}
