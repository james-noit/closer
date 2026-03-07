# Hoja de ruta de backend

Fecha de referencia: 2026-03-07

## Estado actual

- Aplicación Spring Boot 3/Eclipse Temurin con Maven estructurada en paquetes `persona` y controladores REST.
- Entidad JPA `Persona` ubicada en `com.closer.backend.persona.domain` con campos básicos (nombre, apellidos, teléfono, fecha de nacimiento, email) y validaciones.
- Servicio `PersonaService` y su implementación `PersonaServiceImpl` para lógica de negocio.
- Ensamblador `PersonaModelAssembler` y controlador `PersonaController` exportan una API CRUD de personas.
- Configuraciones en `src/main/resources` con perfiles `desarrollo`, `local`, `produccion` y `test`.
- Pruebas unitarias/integración bajo `src/test/java/com/closer/backend` verificando el funcionamiento de la aplicación (e.g. `CloserBackendApplicationTests`).
- Ficheros DML iniciales (`personas-*.sql`) para poblado de datos.
- Construcción produciendo JAR en `target/` y estructura generada por Maven.

## Pendiente inmediato

- Diseñar e implementar la entidad `Usuario` y su relación con personas (ver nota de diseño abajo).  
  (realizado: clases, repositorio, servicio, controlador y manejo de excepciones añadidos)
- Añadir autenticación/autorización (login) y endpoints relacionados.
- Crear servicios y repositorios para `Usuario`.
- Integrar con el frontend mediante contratos API y pruebas de integración.
- Completar el manejo de excepciones centralizado y validaciones comunes.

## Siguientes pasos sugeridos

1. Definir claramente el dominio de `Usuario` versus `Persona` y decidir modelado (composición vs. herencia).
2. Añadir controladores de seguridad (JWT, OAuth, etc.) y pruebas de seguridad.
3. Extender la base de datos con tablas de usuarios, roles y permisos.
4. Documentar la API con Swagger/OpenAPI.
5. Añadir pruebas de contrato para la comunicación con el frontend.
6. Preparar despliegues para los distintos perfiles y entornos.

> **Nota de diseño**: `Usuario` comparte información con `Persona` pero no es estrictamente un subtipo lógico de persona; suele representar la cuenta de acceso. En lugar de heredar, es preferible modelar una relación uno‑a‑uno y mantener separadas las tablas, para evitar acoplamientos de seguridad y facilitar la gestión de permisos. Si el dominio exige un `Usuario` que *sea* una persona (p.ej. todos los usuarios son personas y se utilizan indistintamente), la herencia JPA (`@Inheritance`) podría emplearse, aunque añade complejidad en la base de datos. En general, se recomienda composición/relación sobre herencia por claridad y flexibilidad.