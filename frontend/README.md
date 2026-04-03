# Frontend De TaskFlow

Frontend Angular de TaskFlow, una aplicacion de gestion de tareas con enfoque SaaS, construida con standalone components, TypeScript, SCSS y soporte SSR.

Este workspace se encarga de la experiencia de usuario completa: autenticacion, shell principal, dashboard, tareas, categorias, area admin y tratamiento coherente de fechas, horas y zona horaria.

## Objetivo Del Frontend

La interfaz esta planteada para transmitir una sensacion de producto real:

- visual oscuro y sobrio
- jerarquia clara
- componentes consistentes
- UX limpia para tareas, formularios y administracion
- acabado adecuado para portfolio

## Stack

- Angular 19
- TypeScript
- SCSS
- Angular SSR
- RxJS
- Arquitectura standalone

## Responsabilidades Principales

- login, registro y rehidratacion de sesion
- shell autenticado con sidebar y topbar
- dashboard del usuario
- CRUD de tareas
- gestion de categorias
- panel admin
- toasts, confirmaciones y estados vacios
- deteccion de timezone del navegador
- conversion entre hora local del usuario y UTC para backend

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

- `models`: contratos tipados con la API
- `services`: acceso a backend y logica cliente
- `guards`: proteccion de rutas autenticadas y admin
- `interceptors`: inyeccion automatica del JWT

### Features

- `auth`: login y registro
- `dashboard`: resumen del espacio de trabajo
- `tareas`: listado, filtros, cards y formulario
- `categorias`: organizacion de tareas
- `admin`: gestion de usuarios, metricas y recordatorios fallidos

### Layout

- `main-layout`: shell principal autenticado con navegacion sensible al rol

### Shared

- componentes reutilizables para loading, empty states, dialogos de confirmacion, notificaciones y rutas no encontradas

## Sesion Y Seguridad En Frontend

`AuthService` es la fuente de verdad de la sesion actual.

Se encarga de:

- guardar la sesion en local storage
- exponer el usuario autenticado
- rehidratar sesiones previas
- mantener nombre, email, timezone y roles
- permitir que guards y layout reaccionen al rol actual

Guards principales:

- `guestGuard`
- `authGuard`
- `adminGuard`

## Fechas, Horas Y Zona Horaria

La aplicacion trabaja con una estrategia clara:

- el usuario introduce fecha y hora en su contexto local
- el frontend detecta la timezone del navegador
- `TimezoneService` transforma los valores de `datetime-local` a UTC antes de enviarlos
- los valores UTC recibidos del backend se vuelven a presentar en hora local para edicion y visualizacion

Esto evita inconsistencias entre navegador, backend y base de datos.

## Semantica Actual De Tareas

El frontend esta alineado con una logica funcional clara:

- `fechaInicio`: cuando deberia empezar la tarea
- `fechaLimite`: cuando deberia terminar como maximo
- `recordatorio`: aviso previo calculado respecto a la fecha de inicio

La UI intenta dejar esto evidente en:

- formulario de creacion y edicion
- cards de tareas
- dashboard
- filtros y listados

## Experiencia Admin

Los usuarios con rol admin ven una zona adicional integrada en el shell:

- acceso visible desde navegacion principal
- resumen global del sistema
- listado de usuarios con filtros
- acciones de activacion, desactivacion y rol
- creacion de usuarios
- visualizacion de fallos recientes de recordatorios

La interfaz admin mantiene el mismo sistema visual del resto del producto y no se presenta como una pantalla desconectada.

## Design System

El design system base vive en `src/styles.scss`.

Incluye:

- tokens de color
- superficies oscuras
- espaciado consistente
- radios y sombras
- estilos base de botones
- inputs, selects y estados focus
- badges, cards y paneles reutilizables

El objetivo es mantener coherencia visual entre todos los flujos sin depender de una libreria de componentes externa.

## Desarrollo Local

### Requisitos

- Node.js 20 o superior recomendado
- npm
- backend de TaskFlow disponible en `http://localhost:8080`

### Instalar Dependencias

```bash
npm install
```

### Iniciar Desarrollo

```bash
npm start
```

Aplicacion disponible en:

```text
http://localhost:4200
```

## Scripts

| Comando | Uso |
| --- | --- |
| `npm start` | Servidor de desarrollo |
| `npm run build` | Build de produccion |
| `npm run watch` | Build en modo observacion |
| `npm test` | Tests de frontend |
| `npm run serve:ssr:frontend` | Servir build SSR |

## Integracion Con El Backend

El frontend consume el backend de TaskFlow en:

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

## Notas Sobre Recordatorios

El frontend soporta:

- activar o desactivar recordatorio
- elegir minutos antes del inicio
- visualizar cuando se enviara el aviso

El envio real del correo depende del backend y de la configuracion del proveedor de email. En local, lo normal es trabajar con proveedor `log` y usar SendGrid solo cuando exista remitente verificado.

## Verificaciones

```bash
npm run build
npm test
```

## Documentacion Relacionada

- [README raiz del proyecto](../README.md)

