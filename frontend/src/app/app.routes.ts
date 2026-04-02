import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guards';

export const appRoutes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'tareas',
        loadComponent: () =>
          import('./features/tareas/tarea-list/tarea-list.component').then(m => m.TareaListComponent)
      },
      {
        path: 'tareas/nueva',
        loadComponent: () =>
          import('./features/tareas/tarea-form/tarea-form.component').then(m => m.TareaFormComponent)
      },
      {
        path: 'tareas/editar/:id',
        loadComponent: () =>
          import('./features/tareas/tarea-form/tarea-form.component').then(m => m.TareaFormComponent)
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/categorias/categorias-list/categoria-list.component').then(
            m => m.CategoriaListComponent
          )
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      },
      {
        path: '**',
        loadComponent: () =>
          import('./shared/components/pages/not-found/not-found.component').then(
            m => m.NotFoundComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
