import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  constructor(public auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.loadDiscovery();
  }

  logout(): void {
    this.auth.logout();
  }
}
