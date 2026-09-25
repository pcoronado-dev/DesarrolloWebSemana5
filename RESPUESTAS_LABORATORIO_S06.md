# Laboratorio 06 - Seguridad Básica sobre API REST (AOP)

**Proyecto:** `productos-api` — API REST de Productos con AOP + control de acceso
**Stack:** Spring Boot 4.1.1 · Java · Spring Data JPA · MySQL · Spring AOP (`spring-boot-starter-aspectj`)
**Paquete base:** `com.tecsup`

---

## 1. Qué se agregó en esta versión (v2)

Sobre la base del laboratorio de AOP se implementó **seguridad básica por roles**:

1. Campo `usuario` en `auditoria_log` (quién hizo la operación).
2. `ForbiddenException` (403) y `UnauthorizedException` (401).
3. Control de acceso por headers `Usuario` y `Rol`, aplicado con `@Before` en
   `AuditoriaAspect`.
4. `GlobalExceptionHandler` mapea 401 / 403.
5. Auditoría dinámica con usuario y parámetros reales del método.

## 2. Estructura

```
src/main/java/com/tecsup
├── aspect
│   ├── AuditoriaAspect.java   ← validación de roles + auditoría con usuario
│   ├── ErrorAspect.java       ← solo consola (sin cambios en esta version)
│   └── LoggingAspect.java     ← log en consola (sin cambios en esta version)
├── exception
│   ├── ForbiddenException.java    ← nueva (403)
│   ├── UnauthorizedException.java ← nueva (401)
│   └── GlobalExceptionHandler.java ← + handlers 401 / 403
├── model
│   ├── AuditoriaLog.java      ← + campo usuario
│   └── Producto.java
├── repository
│   ├── AuditoriaRepository.java   (sin cambios)
│   └── ProductoRepository.java
└── service
    ├── AuditoriaService.java  ← registrar(accion, metodo, detalle, usuario)
    └── ProductoService.java   ← guardar / listar / actualizar / eliminar
```

## 3. Base de datos

`auditoria_log` con la columna nueva (Hibernate la añade con `ddl-auto=update`; DDL y
`ALTER TABLE` de referencia en `scripts/laboratorio.sql`):

```sql
ALTER TABLE auditoria_log ADD usuario VARCHAR(100);
```

## 4. Usuarios y roles (centralizados en el aspecto)

| Usuario | Rol   |
|---------|-------|
| Ricardo | ADMIN |
| Ana     | USER  |
| Luis    | USER  |

## 5. Control de acceso

Los headers se envían en cada petición: `Usuario` y `Rol`.

| Método del servicio | Operación   | Roles permitidos |
|---------------------|-------------|------------------|
| `guardar`           | CREAR       | ADMIN y USER *  |
| `listar`            | LISTAR      | ADMIN y USER     |
| `actualizar`        | ACTUALIZAR  | ADMIN            |
| `eliminar`          | ELIMINAR    | ADMIN            |

\* Actividad: USER también puede crear productos.

- Sin `Usuario`/`Rol` → **401** "Debe enviar Usuario y Rol".
- Usuario no registrado o rol que no coincide → **403** "No tiene permisos".
- Rol válido pero sin permiso para la operación → **403** "Acceso denegado".

## 6. Auditoría registrada

| Acción    | Detalle (dinámico)                |
|-----------|-----------------------------------|
| CREAR     | `Se creó producto: nombre=..., precio=...` |
| ACTUALIZAR| `Se actualizó producto: <id>`     |
| ELIMINAR  | `Se eliminó producto ID: <id>`    |
| LISTAR    | `Se listaron N producto(s)`       |

Todos los registros incluyen el **usuario** que ejecutó la acción (`Ricardo (ADMIN)`, `Ana (USER)`, etc.).

## 7. Cómo probar (POSTMAN)

```powershell
# 1) MySQL encendido en XAMPP +  .\mvnw.cmd spring-boot:run

# 2) Peticiones con headers Usuario / Rol:
#    POST   /api/productos     (Ricardo/ADMIN o Ana/USER)   -> 201
#    GET    /api/productos     (Ricardo/ADMIN o Ana/USER)   -> 200
#    PUT    /api/productos/5   (solo ADMIN)                 -> 200
#    DELETE /api/productos/5   (solo ADMIN)                 -> 200
#    Sin headers                                             -> 401
#    Ricardo con Rol USER (no coincide su rol)               -> 403

# 3) Verificar auditoría
& "C:\xampp\mysql\bin\mysql.exe" -u root laboratorio_api -e "SELECT * FROM auditoria_log ORDER BY id DESC;"
```

Pruebas automatizadas con H2:

```powershell
.\mvnw.cmd test   # 10 pruebas: 4 controller + 1 auditoria + 4 seguridad + 1 contexto
```

---

## OBSERVACIONES

1. MySQL de XAMPP a veces queda apagado o con el log InnoDB corrupto si se cierra de golpe;
   la app no arranca con "Connection refused / unable to determine Dialect" y no es un problema
   de código. Se resuelve reiniciando `mysqld`.

2. En Spring Boot 4.x el starter de AOP es `spring-boot-starter-aspectj`, no
   `spring-boot-starter-aop` (este último ya no lo gestiona el parent).

3. El `@Autowired HttpServletRequest` dentro del aspecto funciona porque Spring inyecta un
   proxy request-scoped; los headers `Usuario`/`Rol` se leen del request actual de cada
   petición, incluso bajo MockMvc en las pruebas.

4. Los advice `@Before` lanzan `UnauthorizedException`/`ForbiddenException` **antes** de que
   corra el método del servicio, y el `GlobalExceptionHandler` los convierte en 401/403 con un
   mensaje claro; es la clave del control de acceso sin tocar el controlador.

5. Los pointcuts se acotaron a `ProductoService.*(..)` para no capturar también a
   `AuditoriaService` y evitar ruido/recursión en los logs del aspecto.

6. Los registros anteriores a añadir `usuario` quedaron con `NULL` en esa columna (Hibernate
   solo agrega la columna, no rellena las filas viejas); es un comportamiento normal.

## CONCLUSIONES

1. Con AOP puse la seguridad en un solo lugar: el `@Before` valida roles y el `@AfterReturning`
   audita, sin modificar ni el controlador ni la lógica del servicio.

2. Centralizar los usuarios en un `Map` fue simple y suficiente para el laboratorio; en un
   sistema real se reemplazaría por una tabla de usuarios en BD.

3. Separar 401 (falta de credenciales) de 403 (sin permisos) dio respuestas de error claras
   y correctas en el `GlobalExceptionHandler`.

4. La auditoría quedó más útil al registrar quién hizo qué: ahora cada fila dice la acción,
   el detalle dinámico y el usuario (`Ana (USER)`, `Ricardo (ADMIN)`).

5. Cumplí la actividad: USER crea y lista, MANUAL de roles: solo ADMIN actualiza/elimina, y
   `listar()` quedó auditado con la cantidad. Las 10 pruebas (incluidas 401/403) pasaron y se
   verificó con curls reales contra MySQL.

---

## Archivos creados/modificados en esta versión

- `src/main/java/com/tecsup/model/AuditoriaLog.java` — campo `usuario`
- `src/main/java/com/tecsup/service/AuditoriaService.java` — `registrar(..., usuario)`
- `src/main/java/com/tecsup/exception/ForbiddenException.java` — nuevo
- `src/main/java/com/tecsup/exception/UnauthorizedException.java` — nuevo
- `src/main/java/com/tecsup/exception/GlobalExceptionHandler.java` — handlers 401 / 403
- `src/main/java/com/tecsup/aspect/AuditoriaAspect.java` — control de acceso + auditoría con usuario
- `src/main/java/com/tecsup/aspect/ErrorAspect.java` — solo consola (v2 no lo modifica)
- `scripts/laboratorio.sql` — columna `usuario`
- `src/test/java/com/tecsup/ProductoControllerTest.java` — con headers de credenciales
- `src/test/java/com/tecsup/AuditoriaAspectTest.java` — auditoría con usuario
- `src/test/java/com/tecsup/SeguridadAspectTest.java` — nuevo (401 / 403 / roles)