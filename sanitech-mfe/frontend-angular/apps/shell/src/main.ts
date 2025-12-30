import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { AppComponent } from './app.component';
const routes: Routes = [ { path: '', component: AppComponent } ];
bootstrapApplication(AppComponent, { providers: [ provideRouter(routes), provideHttpClient() ] });
