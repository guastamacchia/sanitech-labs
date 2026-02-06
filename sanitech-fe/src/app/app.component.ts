import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgIf],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  constructor(public auth: AuthService, private router: Router) {}

  async ngOnInit(): Promise<void> {
    const discoveryLoaded = await this.auth.loadDiscovery();
    if (!discoveryLoaded) {
      return;
    }
    if (this.auth.isAuthenticated) {
      const currentUrl = this.router.url;
      const isPublicRoute = currentUrl === '/' || !currentUrl.startsWith('/portal');
      if (isPublicRoute) {
        await this.router.navigate(['/portal']);
      }
    }
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  get isInReservedArea(): boolean {
    return this.router.url.startsWith('/portal');
  }
}
