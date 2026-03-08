# Matriz de permisos Postman

El siguiente cuadro describe cada petición de la colección `persona-crud.postman_collection.json` junto con los roles requeridos y el código de estado esperado cuando se ejecuta con un token válido.

| Nombre de la petición | Método | URI | Rol necesario | Códigos de respuesta esperados |
|-----------------------|--------|-----|---------------|-------------------------------|
| Auth - Login          | POST   | /api/auth/login  | ninguno (público) | 200 
| Auth - Refresh        | POST   | /api/auth/refresh | usuario autenticado | 200, 401 en caso de refres token inválido |
| Persona - Crear       | POST   | /api/personas     | ROLE_LOGGED_USER o ROLE_ADMIN | 201 |
| Persona - Listar      | GET    | /api/personas     | ROLE_LOGGED_USER (solo propias) o ROLE_ADMIN | 200 (200 con matriz vacía para otro usuario) |
| Persona - Obtener por ID | GET | /api/personas/{{personaId}} | ROLE_LOGGED_USER (propia) o ROLE_ADMIN | 200, 403 si intenta ver ajena |
| Persona - Actualizar  | PUT    | /api/personas/{{personaId}} | ROLE_LOGGED_USER (propia) o ROLE_ADMIN | 200, 403 si intenta edición ajena |
| Persona - Eliminar    | DELETE | /api/personas/{{personaId}} | ROLE_LOGGED_USER (propia) o ROLE_ADMIN | 204, 403 si intenta borrar ajena |
| Usuario - Crear       | POST   | /api/usuarios     | ROLE_ADMIN | 201, 403 para otros roles |
| Usuario - Listar      | GET    | /api/usuarios     | ROLE_ADMIN | 200, 403 para ROLE_LOGGED_USER |
| Usuario - Obtener por ID | GET | /api/usuarios/{{usuarioId}} | ROLE_ADMIN (cualquiera) o ROLE_LOGGED_USER (solo propio) | 200, 403 si ROLE_LOGGED_USER ve otro |
| Usuario - Actualizar  | PUT    | /api/usuarios/{{usuarioId}} | ROLE_ADMIN | 200, 403 otro |
| Usuario - Eliminar    | DELETE | /api/usuarios/{{usuarioId}} | ROLE_ADMIN | 204, 403 otro |
| Auth - Logout         | POST   | /api/auth/logout  | usuario autenticado | 204 |

> **Nota:** para generar un token con un rol determinado, haga login con la cuenta apropiada (`localuser` para ROLE_LOGGED_USER, `admin` para ROLE_ADMIN).


Esta matriz puede consultarse al ejecutar la colección para verificar inmediatamente si la aplicación responde conforme a la política de roles.