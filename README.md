# Monitoring Priority Tasks API

Backend REST desarrollado con **Java 21** y **Spring Boot 3** para gestionar tareas por prioridad, con autenticacion JWT, control por roles y pruebas unitarias.

## Perfil del proyecto (Java Junior Developer)

Este proyecto representa mi enfoque como desarrollador Java Junior:
- construir APIs mantenibles con arquitectura por capas
- aplicar principios SOLID y buenas practicas de Clean Code
- validar reglas de negocio con tests unitarios
- documentar y automatizar calidad con cobertura de pruebas

## Features principales

- Registro e inicio de sesion de usuarios
- Autorizacion por rol (`ROLE_ADMIN`, `ROLE_USER`)
- CRUD de tareas con prioridad
- CRUD de categorias
- Manejo global de excepciones con respuestas consistentes
- Documentacion de API con Swagger/OpenAPI

## Tech stack (palabras clave ATS)

- `Java 21`
- `Spring Boot 3`
- `Spring Web`
- `Spring Data JPA`
- `Spring Security`
- `JWT (io.jsonwebtoken)`
- `PostgreSQL` (entorno real)
- `MapStruct`
- `Lombok`
- `JUnit 5`
- `Mockito`
- `JaCoCo`
- `Maven`
- `REST API`
- `SOLID`
- `Clean Code`

## Arquitectura y buenas practicas

- Arquitectura por capas: `controller`, `service`, `repository`, `mapper`, `dto`.
- Validaciones con Bean Validation (`@Valid`).
- Separacion de responsabilidades para mantener servicios limpios y testeables.
- Excepciones de negocio y de recurso centralizadas en `GlobalExceptionHandler`.
- Mapeo entidad-DTO con MapStruct para reducir codigo boilerplate.

## Endpoints principales

- `/api/auth`
  - `POST /login`
  - `POST /register`
  - `GET /users` (solo admin)
- `/api/users`
- `/api/tasks`
- `/api/categories`

## Reglas de negocio clave

- Cada usuario nuevo se registra con rol `ROLE_USER` por defecto.
- El `email` y `username` de usuario deben ser unicos.
- Las categorias deben tener nombre unico.
- Las tareas requieren: titulo, fecha de vencimiento, categoria y usuario.
- La fecha de vencimiento de una tarea no puede estar en el pasado.
- La prioridad de tarea se valida entre `1` y `10`.
- Un usuario `ROLE_USER` solo puede consultar/modificar sus propios recursos.
- Un usuario `ROLE_ADMIN` puede administrar usuarios, categorias y ver recursos globales.

## Seguridad (JWT) paso a paso

1. El cliente hace `POST /api/auth/login` con `username` y `password`.
2. Si las credenciales son validas, la API devuelve un `token` JWT.
3. En requests protegidas, el cliente envia el token en el header:
   - `Authorization: Bearer <token>`
4. `JwtRequestFilter` valida el token en cada request.
5. Si el token es valido, Spring Security carga el usuario autenticado y aplica las reglas de `@PreAuthorize`.

Rutas publicas:
- `POST /api/auth/login`
- `POST /api/auth/register`
- Swagger (`/v3/api-docs/**`, `/swagger-ui/**`)

Resto de rutas:
- requieren autenticacion JWT

## Ejemplos de requests y responses

### 1) Login

**Request** `POST /api/auth/login`

```json
{
  "username": "davidrt301",
  "password": "secret123"
}
```

**Response** `200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "davidrt301"
}
```

### 2) Registro de usuario

**Request** `POST /api/auth/register`

```json
{
  "username": "nuevo_user01",
  "email": "nuevo@email.com",
  "password": "pass1234"
}
```

**Response** `201 Created`

```json
{}
```

### 3) Crear categoria (solo admin)

**Request** `POST /api/categories`

```json
{
  "name": "Trabajo"
}
```

**Response** `201 Created`

```json
{
  "id": 1,
  "name": "Trabajo"
}
```

### 4) Crear tarea

**Request** `POST /api/tasks`

```json
{
  "title": "Repasar spring boot",
  "description": "Repasar Java, Spring y testing",
  "expirationDate": "2027-09-15T20:00:00",
  "priority": 8,
  "categoryId": 1,
  "userId": 1
}
```

**Response** `201 Created` (ejemplo)

```json
{
  "id": 10,
  "title": "Repasar spring boot",
  "description": "Repasar Java, Spring y testing",
  "creationDate": "2026-01-15T20:00:00",
  "expirationDate": "2027-09-15T20:00:00",
  "priority": 8,
  "completed": false,
  "categoryName": "Trabajo",
  "username": "davidrt301"
}
```

### 5) Obtener tareas vencidas

**Request** `GET /api/tasks/overdue`

Header:
- `Authorization: Bearer <token>`

**Response** `200 OK`

```json
[
  {
    "id": 3,
    "title": "Entregar reporte",
    "priority": 9,
    "completed": false
  }
]
```

## Como ejecutar en local

### Requisitos
- JDK 21
- Maven 3.9+

> El proyecto usa **PostgreSQL** como base de datos.

### Configuracion
Variables opcionales:
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

### Run con PostgreSQL

Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`, luego ejecuta:
```bash
mvn spring-boot:run
```

Aplicacion:
- `http://localhost:8080`

Swagger:
- `http://localhost:8080/swagger-ui/index.html`

## Testing y cobertura

Ejecutar tests:
```bash
mvn test
```

Generar reporte JaCoCo:
```bash
mvn clean test
```

Reporte HTML:
- `target/site/jacoco/index.html`

El proyecto incluye regla de cobertura minima en JaCoCo para mantener estandar de calidad.

## Lo que demuestra este repositorio

- Desarrollo de API REST con Spring Boot
- Implementacion de autenticacion y autorizacion con Spring Security + JWT
- Modelado por capas orientado a mantenibilidad
- Pruebas unitarias enfocadas en reglas de negocio
- Base solida para evolucionar a tests de integracion y CI/CD

## Proximos pasos (roadmap)

- dockerizar aplicacion y base de datos
- agregar ejemplos de request/response por endpoint
