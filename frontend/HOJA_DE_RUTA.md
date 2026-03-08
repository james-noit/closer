# Hoja de ruta de frontend

Fecha de referencia: 2026-03-08

## Estado actual

- Proyecto base en Angular 21.2.1 inicializado.
- Estructura principal creada con componentes `header` y `footer`.
- Pagina de inicio creada como ruta raiz (`/`).
- Paleta de color definida en `src/app/app.scss`:
  - `--primary` (amarillo crema suave) + variantes `light` y `dark`
  - `--secondary` (verde suave) + variantes `light` y `dark`
  - `--tertiary` (verde-agua suave) + variantes `light` y `dark`
- Grafico mock en la home con 5 columnas y mapa de calor de izquierda a derecha (verde a rojo).
- Carpeta `src/app/models` creada con el modelo `contacto`.
- Archivos de entorno (`src/environments/environment.ts` y `.prod.ts`) configurados con `apiUrl`.
- Flujo de autenticacion implementado:
  - `AuthService` con login, logout, refresh y gestion de tokens en `localStorage`.
  - `LoginComponent` standalone con formulario reactivo, spinner de carga en boton y tarjeta de error animada (muestra `title` y `detail` del `ProblemDetail` del backend).
  - `AuthGuard` protege todas las rutas excepto `/login`, con `returnUrl`.
  - Header muestra Login/Logout condicionalmente segun estado de sesion.
- Interceptor HTTP centralizado en `src/app/core/interceptors/http.interceptor.ts`:
  - Adjunta token JWT a peticiones salientes (excepto `/auth/*`).
  - Intenta refresh transparente ante 401 en endpoints protegidos.
  - Propaga errores al componente para gestion local (tarjeta en login, toasts futuros).

## Pendiente inmediato

- Conectar el grafico a datos reales cuando exista fuente de datos.
- Implementar sistema de toasts globales para errores HTTP fuera de login.
- Añadir pruebas unitarias para `LoginComponent`, `AuthGuard` y el interceptor.

## Siguientes pasos sugeridos

1. Definir servicios de datos y contratos API (PersonaService, UsuarioService).
2. Sustituir mocks del grafico por datos reales y estados de carga.
3. Añadir pruebas de integracion para rutas y componentes principales.
4. Implementar toasts globales para feedback de errores HTTP.
