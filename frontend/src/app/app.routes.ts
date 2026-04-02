import { Routes } from '@angular/router';

export const appRoutes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
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
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
