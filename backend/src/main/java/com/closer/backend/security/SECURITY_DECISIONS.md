# SECURITY_DECISIONS

Fecha: 2026-03-08

## Resumen de decisiones

1. Autenticacion stateless con JWT de acceso.

- Header obligatorio: `Authorization: Bearer <accessToken>`.
- No se usan sesiones de servidor.

1. Refresh token persistido con rotacion.

- Los refresh tokens se guardan en tabla `refresh_tokens` usando hash SHA-256.
- Cada refresh token es de un solo uso: al refrescar, el token anterior se revoca.
- En login se revocan refresh tokens activos previos del mismo usuario.

1. Estrategia de expiracion/revocacion.

- Access token: 1 hora.
- Refresh token: 7 dias.
- Revocacion explicita en logout y revocacion implicita en login/rotacion.

1. Proteccion del login.

- Rate limiting por IP para `/api/auth/login`.
- Lockout temporal por usuario tras multiples intentos fallidos.
- Errores diferenciados: 401 (credenciales), 423 (bloqueo), 429 (rate limit).

1. Respuesta uniforme de errores.

- Se usa `ProblemDetail` en handlers globales para facilitar trazabilidad desde frontend y tests.

1. Modelo de roles aplicado.

- `ROLE_UNKNOWN_USER`: contexto anónimo (sin token o no autenticado).
- `ROLE_LOGGED_USER`: usuario autenticado estándar.
- `ROLE_ADMIN`: administración global del sistema.
- Las autoridades salen del campo persistido `usuarios.role`.

## Criterios de aceptacion por endpoint

### POST `/api/auth/login`

- Dado usuario/password validos, responde `200` y body con `token`, `refreshToken`, `tokenType=Bearer`, `expiresIn`.
- Dado usuario/password invalidos, responde `401` con `ProblemDetail` de credenciales.
- Dado usuario bloqueado temporalmente, responde `423`.
- Dado exceso de intentos por IP, responde `429`.

### POST `/api/auth/refresh`

- Dado refresh token activo y no expirado, responde `200` con nuevo `token` y nuevo `refreshToken`.
- El refresh token usado queda revocado (one-time token).
- Dado refresh token revocado/expirado/invalido, responde `401`.

### POST `/api/auth/logout`

- Dado refresh token valido o ya revocado, responde `204` (idempotente).
- El refresh token queda revocado si existia activo.

### Endpoints protegidos (`/api/personas/**`, `/api/usuarios/**`)

- `/api/personas/**`
- `ROLE_LOGGED_USER`: acceso a sus propias personas (owner-based).
- `ROLE_ADMIN`: acceso global a todas las personas.
- `/api/usuarios/**`
- `ROLE_ADMIN`: CRUD completo de usuarios.
- `ROLE_LOGGED_USER`: solo lectura de su propio usuario por `id`.
- Sin token o token invalido: `401` o `403` segun punto de fallo de seguridad.

## Politica de logging aplicada

Se registran eventos de seguridad sin volcar secretos.

- `INFO`: login exitoso, refresh exitoso, logout con revocacion.
- `WARN`: credenciales invalidas, lockout, rate limit excedido, refresh invalido, token JWT mal formado.
- `ERROR`: errores no controlados.

No se registran contraseñas ni tokens en texto plano.

## Integracion con frontend

1. CORS configurado en `SecurityConfig`:
   - Origen permitido: `http://localhost:4200` (desarrollo).
   - Metodos: GET, POST, PUT, DELETE, OPTIONS.
   - Credenciales permitidas (`allowCredentials=true`).

2. Interceptor HTTP centralizado en el frontend (`core/interceptors/http.interceptor.ts`):
   - Adjunta `Authorization: Bearer <token>` a todas las peticiones excepto `/auth/*`.
   - Ante un `401` en endpoint protegido, intenta refresh transparente una vez.
   - Si el refresh falla, limpia tokens y fuerza logout.
   - Errores de `/auth/*` (login, refresh, logout) se propagan al componente para gestion local.

3. Gestion de errores en login:
   - El frontend muestra `title` y `detail` del `ProblemDetail` devuelto por el backend.
   - Codigos gestionados: `401` (credenciales), `423` (bloqueo), `429` (rate limit).
