# Frontend De TaskFlow

Frontend Angular de TaskFlow, una aplicación de gestión de tareas con estética premium, construida con standalone components, SCSS y soporte SSR.

Este workspace se encarga del shell autenticado, el dashboard, los flujos de tareas, la gestión de categorías y la experiencia admin. Está diseñado para sentirse como una interfaz SaaS real: oscura, minimalista, consistente y con nivel portfolio.

## Stack

- Angular 19
- TypeScript
- SCSS
- Angular SSR
- RxJS
- Arquitectura Angular standalone

## Responsabilidades Del Frontend

- Flujos de autenticación (`login`, `register`, rehidratación de sesión)
- Shell principal de la aplicación y navegación
- Dashboard con resumen de tareas y trabajo reciente
- CRUD de tareas, filtros, estados de prioridad y configuración de recordatorios
- Gestión de categorías
- Panel admin para usuarios, métricas y fallos de recordatorios
- Detección de zona horaria y conversión de fechas límite

## Desarrollo

### Requisitos

- Node.js 20+ recomendado
- npm
- Backend de TaskFlow ejecutándose en `http://localhost:8080`

### Instalar dependencias

```bash
npm install
```

### Iniciar servidor de desarrollo

```bash
npm start
```

La aplicación queda disponible en:

```text
http://localhost:4200
```

## Scripts Disponibles

| Comando | Propósito |
| --- | --- |
| `npm start` | Arranca el servidor de desarrollo de Angular |
| `npm run build` | Build de producción |
| `npm run watch` | Build en modo watch |
| `npm test` | Ejecuta tests con Karma |
| `npm run serve:ssr:frontend` | Sirve el build SSR desde `dist/frontend` |

## Arquitectura

```text
src/app/
|-- core/
|   |-- guards/
|   |-- interceptors/
|   |-- models/
|   `-- services/
|-- features/
|   |-- admin/
|   |-- auth/
|   |-- categorias/
|   |-- dashboard/
|   `-- tareas/
|-- layout/
|   `-- main-layout/
`-- shared/
    `-- components/
```

### Core

- `models`: contratos tipados de la API
- `services`: acceso a API y orquestación del lado cliente
- `guards`: protección de rutas autenticadas y admin
- `interceptors`: inserción automática del JWT en requests autenticadas

### Features

- `auth`: pantallas de login y registro
- `dashboard`: vista resumen del usuario actual
- `tareas`: listado, filtros, cards y formularios de tareas
- `categorias`: CRUD de categorías
- `admin`: panel de administración con ciclo de vida de usuarios y monitorización

### Layout

- `main-layout`: shell autenticado con sidebar, topbar, cuenta y navegación adaptada a admin

### Shared

- componentes reutilizables para loading, empty states, confirm dialogs, toasts y not-found

## Routing

Rutas principales definidas en `src/app/app.routes.ts`:

- `/login`
- `/register`
- `/dashboard`
- `/tareas`
- `/tareas/nueva`
- `/tareas/editar/:id`
- `/categorias`
- `/admin`

Guards:

- `guestGuard`: bloquea pantallas de auth si ya hay sesión iniciada
- `authGuard`: protege el shell autenticado
- `adminGuard`: restringe `/admin` a `ROLE_ADMIN`

Las rutas SSR se definen en `src/app/app.routes.server.ts`.

## Estado Y Flujo De Datos

TaskFlow mantiene el estado cliente deliberadamente simple:

- Angular signals para estado local de vista
- servicios para llamadas a API y gestión de sesión
- modelos tipados de request y response
- sin librería externa de estado global

Esto hace que el proyecto sea más legible en contexto portfolio sin renunciar a un enfoque serio.

## Modelo De Auth Y Sesión

`AuthService` es la fuente de verdad de la sesión actual.

Se encarga de:

- guardar la sesión JWT en local storage
- exponer señales de autenticación y roles
- rehidratar sesiones antiguas al arrancar la app mediante `/api/auth/me`
- exponer nombre, email, zona horaria y roles del usuario actual

El interceptor HTTP añade el JWT a los requests salientes de forma automática.

## Estrategia De Zona Horaria

El frontend usa `TimezoneService` para mantener consistente el tratamiento de fechas límite:

- detectar la zona horaria del navegador
- convertir valores locales de `datetime-local` a ISO UTC antes de enviarlos al backend
- convertir valores UTC devueltos por la API a formato local para edición
- formatear fechas UTC en cards, previews y vistas admin

Esto encaja con la estrategia del backend, donde las fechas límite se almacenan en UTC y se interpretan en el contexto horario del usuario.

## Design System

El design system está centralizado en `src/styles.scss`.

Define:

- paleta oscura de superficies
- tokens tipográficos
- escala de spacing
- tokens de bordes y sombras
- base de botones, formularios, badges y cards
- utilidades reutilizables para layout y estados de feedback

El objetivo es mantener coherencia entre dashboard, flujos de tareas y herramientas admin sin depender de una librería visual externa.

## Experiencia Admin

La UI admin forma parte del shell principal del producto, no es una pantalla desconectada.

Comportamientos clave:

- entrada admin en el sidebar visible solo para administradores
- contexto visual específico en la topbar
- cards de resumen global
- listado de usuarios con filtros y consumo de API preparado para paginación
- acciones de activación de cuenta y cambio de rol admin
- confirmación antes de eliminación
- monitorización de fallos recientes de recordatorios
- autoprotección en la UI frente a acciones peligrosas sobre la cuenta admin actual

## Integración Con El Backend

Este frontend espera el backend en:

```text
http://localhost:8080
```

Servicios principales:

- `AuthService`
- `TareaService`
- `CategoriaService`
- `AdminService`
- `TimezoneService`
- `ToastService`

## Resultado Del Build

Los builds de producción se generan en:

```text
dist/frontend
```

Este workspace usa el application builder de Angular con salida SSR habilitada.

## Verificaciones De Calidad

Ejecutar build de producción:

```bash
npm run build
```

Ejecutar tests de frontend:

```bash
npm test
```

## Notas Para Contribuir

- Prioriza actualizar tokens compartidos en `src/styles.scss` antes que añadir reglas visuales aisladas
- Reutiliza `core/models` y `core/services` antes de crear contratos locales por feature
- Mantén los flujos admin detrás de guards y UI sensible al rol
- Conserva el flujo de conversión de zona horaria al tocar pantallas relacionadas con fechas límite

## Documentación Relacionada

- [README raíz del proyecto](../README.md)
