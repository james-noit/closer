# Backend Closer (Spring Boot 4)

Backend Maven con API REST HATEOAS para el recurso `Persona`.

## Requisitos

- Java 21
- Maven 3.9+

## Estructura

- `core`: configuracion transversal (seguridad, futura autenticacion bearer token).
- `persona`: logica de negocio del agregado Persona (domain, repository, service, web).
- `tests/postman`: bateria de pruebas importable en Postman.

## Ejecutar en local

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API base: `http://localhost:8080/api/personas`

## Perfiles

- `local`: H2 en fichero (`./data/closer-local`) + seed de 50 personas (`dml/personas-local.sql`).
- `desarrollo`: H2 en memoria + seed de 50 personas (`dml/personas-desarrollo.sql`).
- `produccion`: configuracion preparada para BD externa (variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

Ejemplos:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=produccion
```

Nota: la validacion de H2 se realiza sobre la API (`/api/personas`) comprobando carga y lectura de datos.

## Compilar y testear

```bash
cd backend
mvn clean test
```

Los tests usan perfil `test` con H2 en memoria para evitar bloqueos del fichero local.

## Postman

Importa la coleccion:

- `tests/postman/persona-crud.postman_collection.json`

La coleccion prueba el flujo basico de CRUD:

1. Crear persona
2. Listar personas
3. Obtener por id
4. Actualizar
5. Eliminar
6. Verificar 404 tras eliminar
