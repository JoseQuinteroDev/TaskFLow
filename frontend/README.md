# Frontend De TaskFlow

Aplicación Angular de TaskFlow, una solución de gestión de tareas con enfoque SaaS, construida con componentes standalone, TypeScript, SCSS y soporte SSR.

Este frontend se encarga de la experiencia de usuario completa: autenticación, contenedor principal, panel principal, tareas, categorías, área de administración y tratamiento coherente de fechas, horas y zona horaria.

## Objetivo Del Frontend

La interfaz está planteada para transmitir una sensación de producto real:

- visual oscuro y sobrio
- jerarquía clara
- componentes consistentes
- UX limpia para tareas, formularios y administración
- acabado adecuado para portfolio

## Stack

- Angular 19
- TypeScript
- SCSS
- Renderizado del lado del servidor con Angular
- RxJS
- Arquitectura standalone

## Responsabilidades Principales

- inicio de sesión, registro y rehidratación de sesión
- contenedor autenticado con barra lateral y cabecera superior
- panel principal del usuario
- CRUD de tareas
- gestión de categorías
- panel de administración
- toasts, confirmaciones y estados vacíos
- detección de la zona horaria del navegador
- conversión entre la hora local del usuario y UTC para backend

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
- `services`: acceso a backend y lógica cliente
- `guards`: protección de rutas autenticadas y admin
- `interceptors`: inyección automática del JWT

### Features

- `auth`: login y registro
- `dashboard`: panel principal del espacio de trabajo
- `tareas`: listado, filtros, tarjetas y formulario
- `categorias`: organización de tareas
- `admin`: gestión de usuarios, métricas y recordatorios fallidos

### Layout

- `main-layout`: contenedor principal autenticado con navegación sensible al rol

### Shared

- componentes reutilizables para carga, estados vacíos, diálogos de confirmación, notificaciones y rutas no encontradas

## Sesión Y Seguridad En Frontend

`AuthService` es la fuente de verdad de la sesión actual.

Se encarga de:

- guardar la sesión en local storage
- exponer el usuario autenticado
- rehidratar sesiones previas
- mantener nombre, email, zona horaria y roles
- permitir que guards y layout reaccionen al rol actual

Guards principales:

- `guestGuard`
- `authGuard`
- `adminGuard`

## Fechas, Horas Y Zona Horaria

La aplicación trabaja con una estrategia clara:

- el usuario introduce fecha y hora en su contexto local
- el frontend detecta la zona horaria del navegador
- `TimezoneService` transforma los valores de `datetime-local` a UTC antes de enviarlos
- los valores UTC recibidos del backend se vuelven a presentar en hora local para edición y visualización

Esto evita inconsistencias entre navegador, backend y base de datos.

## Semántica Actual De Tareas

El frontend está alineado con una lógica funcional clara:

- `fechaInicio`: cuando debería empezar la tarea
- `fechaLimite`: cuando debería terminar como máximo
- `recordatorio`: aviso previo calculado respecto a la fecha de inicio

La UI intenta dejar esto evidente en:

- formulario de creación y edición
- tarjetas de tareas
- panel principal
- filtros y listados

## Experiencia Admin

Los usuarios con rol admin ven una zona adicional integrada en el contenedor principal:

- acceso visible desde la navegación principal
- resumen global del sistema
- listado de usuarios con filtros
- acciones de activación, desactivación y rol
- creación de usuarios
- visualización de fallos recientes de recordatorios

La interfaz admin mantiene el mismo sistema visual del resto del producto y no se presenta como una pantalla desconectada.

## Design System

El sistema base de diseño vive en `src/styles.scss`.

Incluye:

- tokens de color
- superficies oscuras
- espaciado consistente
- radios y sombras
- estilos base de botones
- inputs, selects y estados focus
- insignias, tarjetas y paneles reutilizables

El objetivo es mantener coherencia visual entre todos los flujos sin depender de una librería de componentes externa.

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

Aplicación disponible en:

```text
http://localhost:4200
```

## Scripts

| Comando | Uso |
| --- | --- |
| `npm start` | Servidor de desarrollo |
| `npm run build` | Build de producción |
| `npm run watch` | Build en modo observación |
| `npm test` | Tests de frontend |
| `npm run serve:ssr:frontend` | Servir build SSR |

## Integración Con El Backend

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
- visualizar cuándo se enviará el aviso

El envío real del correo depende del backend y de la configuración del proveedor de email. En local, lo normal es trabajar con proveedor `log` y usar SendGrid solo cuando exista un remitente verificado.

## Verificaciones

```bash
npm run build
npm test
```

## Documentación Relacionada

- [README raíz del proyecto](../README.md)
