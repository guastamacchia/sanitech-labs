import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../../../core/services/api.service';
import { AuthService } from '../../../../core/auth/auth.service';
import { ResourcePageState } from './resource-page.state';

@Component({
  selector: 'app-resource-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './resource-page.component.html'
})
export class ResourcePageComponent extends ResourcePageState {
  constructor(route: ActivatedRoute, api: ApiService, auth: AuthService, router: Router) {
    super(route, api, auth, router);
  }
}
