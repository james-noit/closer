# Hoja de ruta de backend

Fecha de referencia: 2026-03-08

## Estado actual

- Aplicación Spring Boot 3/Eclipse Temurin con Maven estructurada en paquetes `persona` y controladores REST.
- Entidad JPA `Persona` ubicada en `com.closer.backend.persona.domain` con campos básicos (nombre, apellidos, teléfono, fecha de nacimiento, email) y validaciones.
- Servicio `PersonaService` y su implementación `PersonaServiceImpl` para lógica de negocio.
- Ensamblador `PersonaModelAssembler` y controlador `PersonaController` exportan una API CRUD de personas.
- Configuraciones en `src/main/resources` con perfiles `desarrollo`, `local`, `produccion` y `test`.
- Pruebas unitarias/integración bajo `src/test/java/com/closer/backend` verificando el funcionamiento de la aplicación (e.g. `CloserBackendApplicationTests`).
- Suites de pruebas Bruno actualizadas: encabezados HTTP corregidos (sin comillas en nombres, tokens formateados correctamente) y cabeceras redundantes eliminadas.
- Seguridad JWT operativa con `SecurityFilterChain` stateless, `JwtAuthenticationFilter` y endpoint `POST /api/auth/login`.
- Configuración CORS global habilitada para aceptar peticiones desde `http://localhost:4200` (corregido tras bloqueo del navegador).
- Flujo de autenticación ampliado con `POST /api/auth/refresh` (rotación de refresh token) y `POST /api/auth/logout` (revocación).
- Estrategia de revocación persistida en tabla `refresh_tokens` con hash SHA-256 (no se guarda el refresh token en claro).
- Protección de login activa: rate limiting por IP y lockout temporal por usuario tras intentos fallidos.
- Manejo centralizado de errores ampliado con respuestas `ProblemDetail` para credenciales inválidas (401) y acceso denegado (403), incluyendo trazas en logs.
- Manejo centralizado ampliado también para `423 Locked` (cuenta bloqueada) y `429 Too Many Requests` (rate limit login).
- `PersonaController` filtra por usuario autenticado (owner-based access) usando contexto de seguridad para evitar acceso cruzado entre usuarios.
- Corregido error `LazyInitializationException` al listar personas: se evita exponer relaciones JPA recursivas/lazy en JSON (`Persona.usuario`, `Usuario.personas`, `Usuario.persona`).
- Modelo de roles implementado en backend con persistencia en `usuarios.role`: `ROLE_LOGGED_USER`, `ROLE_UNKNOWN_USER`, `ROLE_ADMIN`.
- Reglas de autorización activas:
	- `ROLE_ADMIN` gestiona `/api/usuarios/**` y ve todas las personas.
	- `ROLE_LOGGED_USER` gestiona/consulta sus propias personas y solo puede consultar su propio usuario.
	- `ROLE_UNKNOWN_USER` representa acceso anónimo y no entra a endpoints protegidos.
- Colecciones Bruno (`Auth`, `Persona CRUD`, `Usuario CRUD`) adaptadas para JWT con login parametrizable por entorno y prueba de aislamiento entre usuarios.
- Colección Postman corregida y validada en JSON con flujo `login -> refresh -> CRUD -> logout`.
- Los tests de API se han movido fuera de `backend/` a `tests/bruno` y `tests/postman` (nivel raíz, junto a `backend` y `frontend`).
- Pruebas de seguridad en `AuthControllerTest` ampliadas (login, token, refresh, revocación, lockout, rate limit) y en verde.
- Documento de decisiones de seguridad añadido: `src/main/java/com/closer/backend/security/SECURITY_DECISIONS.md`.
- Ficheros DML iniciales (`personas-*.sql`) para poblado de datos.
- Construcción produciendo JAR en `target/` y estructura generada por Maven.

## Pendiente inmediato

- Endurecer política de alta/edición de usuarios para impedir asignación de roles privilegiados fuera de flujos administrativos controlados.
- Definir política de codificación de contraseñas para altas/actualizaciones de usuario (evitar persistir nuevas contraseñas en claro incluso en entorno local).
- Integrar con el frontend mediante contratos API y pruebas de integración.

## Siguientes pasos sugeridos

1. Definir claramente el dominio de `Usuario` versus `Persona` y cerrar definitivamente el modelado (composición vs. herencia).
2. Extender la base de datos con tablas/campos de roles y permisos.
3. Endurecer seguridad JWT: clave externa por entorno, rotación de claves y lista de revocación para access tokens en incidentes.
4. Añadir pruebas de contrato para la comunicación con el frontend.
5. Documentar la API con Swagger/OpenAPI (incluyendo esquemas de `ProblemDetail`).
6. Preparar despliegues para los distintos perfiles y entornos.

> **Nota de diseño**: `Usuario` comparte información con `Persona` pero no es estrictamente un subtipo lógico de persona; suele representar la cuenta de acceso. En lugar de heredar, es preferible modelar una relación uno‑a‑uno y mantener separadas las tablas, para evitar acoplamientos de seguridad y facilitar la gestión de permisos. Si el dominio exige un `Usuario` que *sea* una persona (p.ej. todos los usuarios son personas y se utilizan indistintamente), la herencia JPA (`@Inheritance`) podría emplearse, aunque añade complejidad en la base de datos. En general, se recomienda composición/relación sobre herencia por claridad y flexibilidad.