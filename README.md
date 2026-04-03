# TaskFlow

TaskFlow es una aplicacion full stack de gestion de tareas construida con Angular y Spring Boot, pensada para sentirse como un producto SaaS real y no como una demo CRUD academica.

El proyecto combina una interfaz oscura, limpia y moderna con una arquitectura backend por capas, autenticacion JWT, gestion de usuarios por roles, fechas con zona horaria real, recordatorios preparados para email y un panel de administracion util y mantenible.

## Vision Del Proyecto

El objetivo de TaskFlow no era solo "hacer una app de tareas", sino construir un producto pequeno pero serio, con decisiones tecnicas razonables y una experiencia de usuario que se vea profesional en portfolio.

La aplicacion esta orientada a resolver un flujo de trabajo claro:

- crear tareas con una fecha de inicio real
- definir opcionalmente una fecha limite
- configurar un recordatorio antes del inicio
- organizar el trabajo por categorias y prioridades
- disponer de una zona de administracion para gestionar usuarios y monitorizar el sistema

## Que Incluye El Proyecto

### Funcionalidad Para Usuario

- Registro y login con JWT
- Dashboard con resumen de actividad
- CRUD completo de tareas
- Fecha de inicio con fecha y hora
- Fecha limite opcional con fecha y hora
- Recordatorio antes del inicio de la tarea
- Prioridades y estados de tarea
- Categorias personalizadas
- Zona horaria del usuario detectada desde frontend

### Funcionalidad Para Administracion

- Panel admin protegido por rol
- Listado de usuarios con filtros
- Alta de usuarios desde panel
- Activacion y desactivacion de cuentas
- Promocion y retirada de rol admin
- Eliminacion segura de usuarios
- Resumen global de plataforma
- Consulta de fallos recientes de recordatorios

## Principales Decisiones De Producto

### 1. Fechas Con Sentido Real

TaskFlow ya no trabaja con una unica fecha ambigua.

Cada tarea puede tener:

- `fechaInicio`: momento en el que deberia empezar a realizarse la tarea
- `fechaLimite`: momento maximo en el que deberia quedar terminada

Esto permite una experiencia mas realista:

- una tarea puede tener inicio y no tener limite
- una tarea no puede vencer antes de empezar
- un recordatorio tiene un punto de referencia claro

### 2. Recordatorios Basados En La Fecha De Inicio

El recordatorio no se calcula respecto a una fecha difusa, sino respecto a la `fechaInicio`.

Ejemplo:

- inicio: `07/04/2026 12:00`
- recordatorio: `60 minutos antes`
- envio esperado: `07/04/2026 11:00`

Esto hace que la funcionalidad tenga sentido para el usuario y simplifica la logica de negocio.

### 3. Zona Horaria Real Del Usuario

El frontend detecta la zona horaria del navegador y la sincroniza con backend.

La estrategia seguida es:

- el usuario trabaja en su hora local
- el frontend convierte a UTC antes de enviar
- el backend persiste los instantes en UTC
- el backend guarda tambien la timezone IANA del usuario
- la aplicacion vuelve a presentar las fechas en el contexto horario correcto del usuario

Con esto se evita depender de la hora del servidor y se reducen errores por cambios horarios o conversiones inconsistentes.

## Recordatorios Por Email

TaskFlow ya incluye la base tecnica para enviar recordatorios reales por email, pero conviene distinguir muy bien entre "infraestructura preparada" y "entorno listo para produccion".

### Lo Que Ya Hace El Proyecto

- scheduler de recordatorios en backend
- logica de negocio para detectar tareas que requieren aviso
- persistencia del estado del recordatorio
- registro de fallos
- abstraccion del proveedor de email mediante `EmailSender`
- implementacion preparada para SendGrid

### Lo Que Hace Falta Para Envio Real

Para mandar emails reales hace falta:

- un proveedor configurado, por ejemplo SendGrid
- una API key valida
- un remitente verificado
- idealmente un dominio propio autenticado
- un backend ejecutandose de forma continua

### Estado Actual Recomendado En El Repositorio

El repositorio queda preparado para:

- desarrollo local con proveedor `log`
- pruebas funcionales de la logica de recordatorios
- futura activacion de proveedor real cuando exista dominio o remitente verificado

No se dejan credenciales reales dentro del repositorio.

### Nota Importante Sobre Produccion

Aunque el proyecto soporta el envio real de recordatorios, para una operacion seria hace falta un backend siempre encendido. Si el backend esta apagado, el scheduler no se ejecuta y por tanto no se enviaran recordatorios.

En otras palabras:

- en local funciona mientras el backend esta levantado
- para una demo continua o un uso real hace falta desplegarlo en un entorno siempre activo

## Stack Tecnologico

| Capa | Tecnologia |
| --- | --- |
| Frontend | Angular 19, TypeScript, SCSS, Angular SSR |
| Backend | Spring Boot 3.5, Spring Security, Spring Data JPA, Flyway |
| Base de datos | MySQL 8 |
| Auth | JWT |
| Email | Abstraccion de proveedor, `log` para desarrollo y base preparada para SendGrid |
| Tooling | Maven Wrapper, Docker Compose, npm |

## Arquitectura

### Backend

El backend sigue una arquitectura clasica por capas:

- `controller`: endpoints REST
- `service`: logica de negocio y orquestacion
- `repositories`: acceso a persistencia
- `dto`: contratos de request y response
- `mapper`: mapeo entre entidades y DTOs
- `security`: JWT, filtros y control de acceso
- `config`: configuracion tipada de tiempo, recordatorios, email y CORS

### Frontend

El frontend esta construido con Angular standalone y se organiza en:

- `core`: modelos, guards, servicios, interceptor y timezone
- `features`: auth, dashboard, tareas, categorias y admin
- `layout`: shell autenticado con sidebar y topbar
- `shared`: componentes reutilizables y feedback transversal

### Persistencia Y Esquema

El proyecto usa Flyway para evolucionar el esquema de base de datos y `spring.jpa.hibernate.ddl-auto=validate` para asegurar que las entidades JPA y la base de datos se mantienen alineadas.

Esto fuerza una disciplina mas realista:

- no depender de cambios automaticos de Hibernate
- versionar la evolucion del modelo
- poder arrancar el proyecto desde una base vacia con un esquema consistente

## Modelo Funcional

### Usuario

Cada usuario dispone de:

- nombre
- email
- password cifrada
- estado activo
- roles
- timezone

### Tarea

Cada tarea puede incluir:

- titulo
- descripcion
- prioridad
- estado
- fecha de inicio
- fecha limite opcional
- recordatorio activo o no
- minutos antes del inicio para recordar
- categoria opcional

### Recordatorio

Los recordatorios se modelan como un registro operativo asociado a la tarea, con informacion de:

- canal
- estado de envio
- destinatario
- fecha programada
- fecha real de envio
- error en caso de fallo

## Estructura Del Repositorio

```text
TaskFLow/
|-- src/                       # Backend Spring Boot activo
|-- frontend/                  # Aplicacion Angular
|-- compose.yaml               # Backend + MySQL para entorno local
|-- Dockerfile                 # Imagen del backend
|-- pom.xml                    # Build del backend
|-- README.md                  # Documentacion raiz
```

Notas relevantes:

- el backend actual vive en `src/`
- la carpeta `backend/` sigue presente solo como legado y no forma parte del build actual

## Desarrollo Local

### Requisitos

- Java 17 o superior
- Node.js 20 o superior recomendado
- npm
- Docker y Docker Compose

### 1. Levantar Base De Datos Y Backend Con Docker

```bash
docker compose up --build
```

Servicios resultantes:

- backend: `http://localhost:8080`
- MySQL: `localhost:3307`

### 2. Levantar El Frontend En Desarrollo

```bash
cd frontend
npm install
npm start
```

Frontend disponible en:

```text
http://localhost:4200
```

## Variables De Entorno Importantes

| Variable | Descripcion |
| --- | --- |
| `SERVER_PORT` | Puerto del backend |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexion a MySQL |
| `APP_TIMEZONE` | Fallback de timezone del backend |
| `APP_CORS_ALLOWED_ORIGINS` | Origen permitido para frontend |
| `JWT_SECRET` | Secreto de firma para JWT |
| `JWT_EXPIRATION` | Vida del token |
| `EMAIL_PROVIDER` | Proveedor de email. En local se recomienda `log` |
| `EMAIL_FROM` | Remitente para proveedor real |
| `SENDGRID_API_KEY` | API key de SendGrid |
| `TASK_REMINDERS_ENABLED` | Activa o desactiva el scheduler |
| `TASK_REMINDERS_CRON` | Frecuencia del job de recordatorios |
| `TASK_REMINDERS_SOON_THRESHOLD` | Ventana de busqueda futura |
| `TASK_REMINDERS_OVERDUE_WINDOW` | Tolerancia de recordatorios retrasados |

## Flujo Real De Recordatorios

Para que un recordatorio se envie realmente deben cumplirse todas estas condiciones:

1. La tarea tiene `fechaInicio`
2. La tarea tiene recordatorio activo
3. El usuario esta activo
4. El backend esta corriendo
5. El scheduler esta habilitado
6. El proveedor de email esta correctamente configurado

Si `EMAIL_PROVIDER=log`, el sistema no manda correo real: solo registra el envio en logs.

Ese modo es intencional y es el recomendado para:

- desarrollo local
- demos tecnicas
- pruebas de flujo sin dominio propio

## Calidad Tecnica

El proyecto incluye una base tecnica mas seria que la de una practica simple:

- migraciones Flyway
- validacion de esquema con Hibernate
- seguridad JWT
- control de acceso por rol
- panel admin real
- tests de backend
- build de frontend validado
- separacion clara de responsabilidades

## Comandos Utiles

Backend:

```bash
./mvnw test
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm run build
npm test
```

Docker:

```bash
docker compose up --build
docker compose down
```

## Valor Como Proyecto De Portfolio

TaskFlow funciona bien como portfolio porque demuestra:

- capacidad full stack real
- criterio visual en frontend
- modelado de negocio razonable
- seguridad y roles
- tratamiento serio de fechas y zona horaria
- evolucion de esquema con migraciones
- administracion de usuarios
- preparacion para integraciones externas

No intenta resolver todo, pero si resuelve bien un alcance realista.

## Estado Del Proyecto

TaskFlow queda en un estado final de portfolio muy solido:

- frontend coherente y profesional
- backend completo y mantenible
- administracion funcional
- fechas, horas y recordatorios redisenados con logica clara
- base preparada para proveedor real de email cuando exista remitente o dominio verificado

## Documentacion Relacionada

- [README del frontend](./frontend/README.md)

