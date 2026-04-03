# TaskFlow

TaskFlow es una aplicación de gestión de tareas de pila completa construida con Angular y Spring Boot, pensada para sentirse como un producto SaaS real y no como una demo CRUD académica.

El proyecto combina una interfaz oscura, limpia y moderna con una arquitectura backend por capas, autenticación JWT, gestión de usuarios por roles, fechas con zona horaria real, recordatorios preparados para correo electrónico y un panel de administración útil y mantenible.

## Visión Del Proyecto

El objetivo de TaskFlow no era solo "crear una app de tareas", sino construir un producto pequeño pero serio, con decisiones técnicas razonables y una experiencia de usuario que se vea profesional en portfolio.

La aplicación está orientada a resolver un flujo de trabajo claro:

- crear tareas con una fecha de inicio real
- definir opcionalmente una fecha límite
- configurar un recordatorio antes del inicio
- organizar el trabajo por categorías y prioridades
- disponer de una zona de administración para gestionar usuarios y monitorizar el sistema

## Qué Incluye El Proyecto

### Funcionalidad Para Usuario

- Registro e inicio de sesión con JWT
- Panel principal con resumen de actividad
- CRUD completo de tareas
- Fecha de inicio con fecha y hora
- Fecha límite opcional con fecha y hora
- Recordatorio antes del inicio de la tarea
- Prioridades y estados de tarea
- Categorías personalizadas
- Zona horaria del usuario detectada desde el frontend

### Funcionalidad Para Administración

- Panel de administración protegido por rol
- Listado de usuarios con filtros
- Alta de usuarios desde el panel
- Activación y desactivación de cuentas
- Promoción y retirada del rol de administrador
- Eliminación segura de usuarios
- Resumen global de la plataforma
- Consulta de fallos recientes de recordatorios

## Principales Decisiones De Producto

### 1. Fechas Con Sentido Real

TaskFlow ya no trabaja con una única fecha ambigua.

Cada tarea puede tener:

- `fechaInicio`: momento en el que debería empezar a realizarse la tarea
- `fechaLimite`: momento máximo en el que debería quedar terminada

Esto permite una experiencia más realista:

- una tarea puede tener inicio y no tener límite
- una tarea no puede vencer antes de empezar
- un recordatorio tiene un punto de referencia claro

### 2. Recordatorios Basados En La Fecha De Inicio

El recordatorio no se calcula respecto a una fecha difusa, sino respecto a la `fechaInicio`.

Ejemplo:

- inicio: `07/04/2026 12:00`
- recordatorio: `60 minutos antes`
- envío esperado: `07/04/2026 11:00`

Esto hace que la funcionalidad tenga sentido para el usuario y simplifica la lógica de negocio.

### 3. Zona Horaria Real Del Usuario

El frontend detecta la zona horaria del navegador y la sincroniza con el backend.

La estrategia seguida es:

- el usuario trabaja en su hora local
- el frontend convierte a UTC antes de enviar
- el backend persiste los instantes en UTC
- el backend guarda también la zona horaria IANA del usuario
- la aplicación vuelve a presentar las fechas en el contexto horario correcto del usuario

Con esto se evita depender de la hora del servidor y se reducen errores por cambios horarios o conversiones inconsistentes.

## Recordatorios Por Email

TaskFlow ya incluye la base técnica para enviar recordatorios reales por email, pero conviene distinguir muy bien entre "infraestructura preparada" y "entorno listo para producción".

### Lo Que Ya Hace El Proyecto

- programador de recordatorios en backend
- lógica de negocio para detectar tareas que requieren aviso
- persistencia del estado del recordatorio
- registro de fallos
- abstracción del proveedor de email mediante `EmailSender`
- implementación preparada para SendGrid

### Lo Que Hace Falta Para El Envío Real

Para mandar emails reales hace falta:

- un proveedor configurado, por ejemplo SendGrid
- una API key válida
- un remitente verificado
- idealmente un dominio propio autenticado
- un backend ejecutándose de forma continua

### Estado Actual Recomendado En El Repositorio

El repositorio queda preparado para:

- desarrollo local con proveedor `log`
- pruebas funcionales de la lógica de recordatorios
- futura activación de un proveedor real cuando exista un remitente o dominio verificado

No se dejan credenciales reales dentro del repositorio.

### Nota Importante Sobre Producción

Aunque el proyecto soporta el envío real de recordatorios, para una operación seria hace falta un backend siempre encendido. Si el backend está apagado, el programador no se ejecuta y, por tanto, no se enviarán recordatorios.

En otras palabras:

- en local funciona mientras el backend está levantado
- para una demo continua o un uso real hace falta desplegarlo en un entorno siempre activo

## Stack Tecnológico

| Capa | Tecnología |
| --- | --- |
| Frontend | Angular 19, TypeScript, SCSS, Angular SSR |
| Backend | Spring Boot 3.5, Spring Security, Spring Data JPA, Flyway |
| Base de datos | MySQL 8 |
| Auth | JWT |
| Correo electrónico | Abstracción de proveedor, `log` para desarrollo y base preparada para SendGrid |
| Herramientas | Maven Wrapper, Docker Compose y npm |

## Arquitectura

### Backend

El backend sigue una arquitectura clásica por capas:

- `controller`: endpoints REST
- `service`: lógica de negocio y orquestación
- `repositories`: acceso a persistencia
- `dto`: contratos de request y response
- `mapper`: mapeo entre entidades y DTOs
- `security`: JWT, filtros y control de acceso
- `config`: configuración tipada de tiempo, recordatorios, email y CORS

### Frontend

El frontend está construido con Angular y componentes standalone, y se organiza en:

- `core`: modelos, guards, servicios, interceptor y timezone
- `features`: auth, panel principal, tareas, categorías y admin
- `layout`: contenedor autenticado con barra lateral y cabecera superior
- `shared`: componentes reutilizables y feedback transversal

### Persistencia Y Esquema

El proyecto usa Flyway para evolucionar el esquema de base de datos y `spring.jpa.hibernate.ddl-auto=validate` para asegurar que las entidades JPA y la base de datos se mantienen alineadas.

Esto fuerza una disciplina más realista:

- no depender de cambios automáticos de Hibernate
- versionar la evolución del modelo
- poder arrancar el proyecto desde una base vacía con un esquema consistente

## Modelo Funcional

### Usuario

Cada usuario dispone de:

- nombre
- email
- contraseña cifrada
- estado activo
- roles
- zona horaria

### Tarea

Cada tarea puede incluir:

- título
- descripción
- prioridad
- estado
- fecha de inicio
- fecha límite opcional
- recordatorio activo o no
- minutos antes del inicio para recordar
- categoría opcional

### Recordatorio

Los recordatorios se modelan como un registro operativo asociado a la tarea, con información de:

- canal
- estado de envío
- destinatario
- fecha programada
- fecha real de envío
- error en caso de fallo

## Estructura Del Repositorio

```text
TaskFLow/
|-- src/                       # Backend Spring Boot activo
|-- frontend/                  # Aplicación Angular
|-- compose.yaml               # Backend + MySQL para entorno local
|-- Dockerfile                 # Imagen del backend
|-- pom.xml                    # Compilación del backend
|-- README.md                  # Documentación raíz
```

Notas relevantes:

- el backend actual vive en `src/`
- la carpeta `backend/` sigue presente solo como legado y no forma parte de la compilación actual

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

| Variable | Descripción |
| --- | --- |
| `SERVER_PORT` | Puerto del backend |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexión a MySQL |
| `APP_TIMEZONE` | Fallback de zona horaria del backend |
| `APP_CORS_ALLOWED_ORIGINS` | Origen permitido para frontend |
| `JWT_SECRET` | Secreto de firma para JWT |
| `JWT_EXPIRATION` | Vida del token |
| `EMAIL_PROVIDER` | Proveedor de email. En local se recomienda `log` |
| `EMAIL_FROM` | Remitente para proveedor real |
| `SENDGRID_API_KEY` | API key de SendGrid |
| `TASK_REMINDERS_ENABLED` | Activa o desactiva el programador |
| `TASK_REMINDERS_CRON` | Frecuencia de la tarea programada de recordatorios |
| `TASK_REMINDERS_SOON_THRESHOLD` | Ventana de búsqueda futura |
| `TASK_REMINDERS_OVERDUE_WINDOW` | Tolerancia de recordatorios retrasados |

## Flujo Real De Recordatorios

Para que un recordatorio se envíe realmente deben cumplirse todas estas condiciones:

1. La tarea tiene `fechaInicio`
2. La tarea tiene recordatorio activo
3. El usuario está activo
4. El backend está corriendo
5. El programador está habilitado
6. El proveedor de email está correctamente configurado

Si `EMAIL_PROVIDER=log`, el sistema no manda correo real: solo registra el envío en logs.

Ese modo es intencional y es el recomendado para:

- desarrollo local
- demos técnicas
- pruebas de flujo sin dominio propio

## Calidad Técnica

El proyecto incluye una base técnica más seria que la de una práctica simple:

- migraciones Flyway
- validación de esquema con Hibernate
- seguridad JWT
- control de acceso por rol
- panel de administración real
- tests de backend
- compilación del frontend validada
- separación clara de responsabilidades

## Comandos Útiles

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
- evolución de esquema con migraciones
- administración de usuarios
- preparación para integraciones externas

No intenta resolverlo todo, pero sí resuelve bien un alcance realista.

## Estado Del Proyecto

TaskFlow queda en un estado final de portfolio muy sólido:

- frontend coherente y profesional
- backend completo y mantenible
- administración funcional
- fechas, horas y recordatorios rediseñados con lógica clara
- base preparada para un proveedor real de email cuando exista un remitente o dominio verificado

## Documentación Relacionada

- [README del frontend](./frontend/README.md)
