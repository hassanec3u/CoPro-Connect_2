import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login-page.component';
import { MainLayoutComponent } from './layouts/main-layout.component';
import { DashboardPageComponent } from './pages/dashboard-page.component';
import { HappixPageComponent } from './pages/happix-page.component';
import { StatisticsPageComponent } from './pages/statistics-page.component';
import { AdminPageComponent } from './pages/admin-page.component';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: DashboardPageComponent },
      { path: 'happix', component: HappixPageComponent },
      { path: 'statistiques', component: StatisticsPageComponent },
      { path: 'admin', component: AdminPageComponent, canActivate: [adminGuard] }
    ]
  },
  { path: '**', redirectTo: '' }
];
