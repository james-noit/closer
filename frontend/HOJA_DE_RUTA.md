# Hoja de ruta de frontend

Fecha de referencia: 2026-03-05

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

## Pendiente inmediato

- Implementar flujo de login (sin iniciar aun).
- Conectar el grafico a datos reales cuando exista fuente de datos.
- Ajustar navegacion superior cuando se incorporen nuevas paginas.

## Siguientes pasos sugeridos

1. Crear modulo/pagina de autenticacion (login).
2. Definir servicios de datos y contratos API.
3. Sustituir mocks del grafico por datos reales y estados de carga.
4. Añadir pruebas de integracion para rutas y componentes principales.
