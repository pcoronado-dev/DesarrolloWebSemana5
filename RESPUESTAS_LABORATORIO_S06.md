# Laboratorio 06 - Implementación con AOP

**Proyecto:** `productos-api` — API REST de Productos con Aspect-Oriented Programming
**Stack:** Spring Boot 4.1.1 · Java · Spring Data JPA · MySQL · Spring AOP (`spring-boot-starter-aspectj`)
**Paquete base:** `com.tecsup`

---

## 1. Estructura agregada al proyecto

```
src/main/java/com/tecsup
├── aspect
│   ├── AuditoriaAspect.java   ← audita CREAR / ACTUALIZAR / ELIMINAR / LISTAR
│   ├── ErrorAspect.java       ← registra errores del servicio (consola + BD)
│   └── LoggingAspect.java     ← log en consola de @Before / @AfterReturning
├── model
│   ├── AuditoriaLog.java      ← entidad de la tabla auditoria_log
│   └── Producto.java
├── repository
│   ├── AuditoriaRepository.java
│   └── ProductoRepository.java
├── service
│   ├── AuditoriaService.java  ← registrar(accion, metodo, detalle)
│   └── ProductoService.java   ← se agregó actualizar(Long id, Producto datos)
└── controller / dto / exception  (sin cambios de contrato)
```

Se agregó la dependencia `spring-boot-starter-aspectj` (en Spring Boot 4.x el starter
`spring-boot-starter-aop` fue renombrado a `spring-boot-starter-aspectj`).

## 2. Base de datos

La tabla `auditoria_log` la crea Hibernate automáticamente a partir de la entidad
(`spring.jpa.hibernate.ddl-auto=update`). DDL de referencia en `scripts/laboratorio.sql`:

```sql
CREATE TABLE IF NOT EXISTS auditoria_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accion VARCHAR(50),      -- CREAR | ACTUALIZAR | ELIMINAR | LISTAR | ERROR
    metodo VARCHAR(100),     -- nombre del metodo del servicio (guardar, actualizar, ...)
    fecha TIMESTAMP,         -- LocalDateTime.now() al registrar
    detalle VARCHAR(255)     -- informacion dinamica (ID, cantidad, mensaje de error)
);
```

## 3. Aspectos implementados

### LoggingAspect (paso 3)

```java
@Before("execution(* com.tecsup.service.ProductoService.*(..))")
@AfterReturning("execution(* com.tecsup.service.ProductoService.*(..))")
```

Imprime en consola cuándo inicia y cuándo termina cada método del servicio.

### ErrorAspect (paso 4 + Parte 3)

```java
@AfterThrowing(pointcut = "...ProductoService.*(..)", throwing = "ex")
```

Antes solo mostraba el error en consola. Se extendió para que además lo persista
en `auditoria_log` con acción `ERROR` usando `AuditoriaService`.

### AuditoriaAspect (pasos 8 + Actividad)

| Acción | Pointcut | Detalle |
|--------|----------|---------|
| CREAR | `ProductoService.guardar(..)` | "Se registró un producto" |
| ACTUALIZAR | `ProductoService.actualizar(..)` | "Se actualizó producto con ID: {id}" |
| ELIMINAR | `ProductoService.eliminar(..)` | "Se eliminó producto con ID: {id}" |
| LISTAR | `ProductoService.listar(..)` | "Se listaron {n} producto(s)" |

Los datos son **dinámicos**: el ID y la cantidad se obtienen desde el `JoinPoint`
(parámetros del método) y desde el valor de retorno del advice `@AfterReturning`.

## 4. Cómo se prueba

```powershell
# 1) MySQL/MariaDB encendido en XAMPP

# 2) Ejecutar
.\mvnw.cmd spring-boot:run

# 3) En POSTMAN:
#    POST   /api/productos      {"nombre":"disco duro","precio":2500,"stock":10}
#    GET    /api/productos
#    PUT    /api/productos/1    {"nombre":"disco SSD","precio":2600,"stock":12}
#    DELETE /api/productos/1

# 4) Verificar la auditoria
mysql -u root laboratorio_api -e "SELECT * FROM auditoria_log ORDER BY id DESC;"
```

Las pruebas automatizadas usan H2 en memoria:

```powershell
.\mvnw.cmd test   # 7 pruebas: 4 controller + 2 auditoria + 1 contexto
```

---

## OBSERVACIONES

1. En Spring Boot 4.x el starter de AOP se llama `spring-boot-starter-aspectj`; el clásico
   `spring-boot-starter-aop` ya no es gestionado por el parent y falla con "version missing".
   Fue la primera traba al agregar el aspecto.

2. Los pointcuts del laboratorio apuntan a `com.tecsup.service.*.*(..)`, pero eso incluiría al
   propio `AuditoriaService` y provocaría ruido en el log (y riesgo de recursión en
   `ErrorAspect`). Los acoté a `ProductoService.*(..)` para auditar solo la lógica de negocio.

3. El `@AfterReturning` con `returning = "resultado"` es la clave para el detalle dinámico:
   en `listar()` se calcula `coleccion.size()`; en `actualizar()`/`eliminar()` se extrae el ID
   desde los argumentos del `JoinPoint`. Con texto fijo no se cumplía la Parte 2.

4. El PUT antes reutilizaba `guardar()`; al pasar a `actualizar(Long, Producto)` tuve que
   alinear controlador y servicio para que el advice de ACTUALIZAR tuviera un método propio
   y no se duplicara con el de CREAR.

5. El advice toma los parámetros con `joinPoint.getArgs()` y el primer `Long` es el ID; si el
   bloque de validación del controlador no hubiera protegido el 404, el `actualizar` de un
   producto inexistente lanza `RuntimeException`, que el `ErrorAspect` registra como ERROR.

6. H2 (modo prueba) y MySQL aceptan la tabla igual porque el nombre `auditoria_log` sale
   automáticamente de la entidad `AuditoriaLog` (convención snake_case de Spring Boot).

## CONCLUSIONES

1. AOP permitió cruzar la auditoría y el log por todos los métodos del servicio sin tocar la
   lógica de negocio: los aspectos quedan separados y se activan o desactivan quitando la
   dependencia, sin modificar el controlador.

2. Con `@AfterReturning` + `returning` y `joinPoint.getArgs()` la auditoría quedó dinámica
   (ID real y cantidad de registros), tal como pedía la Parte 2, en vez de mensajes fijos.

3. Persistir los errores en `auditoria_log` (Parte 3) convirtió los fallos en trazables: ya no
   solo aparecen en consola, queda un historial que se puede consultar por SQL.

4. Restringir el pointcut a `ProductoService` evitó recursion sobre `AuditoriaService` y dejó
   los logs limpios; fue un ajuste que el diseño propuesto del laboratorio no contemplaba.

5. `spring-boot-starter-aspectj` + `@Aspect` + `@Component` funcionan de inmediato en Spring
   Boot 4.1; el aspecto se aplica a los beans del contenedor y las pruebas con MockMvc + H2
   confirmaron que cada operación CRUD genera su fila de auditoría y que los errores también
   se registran.

---

## Archivos creados/modificados

- `pom.xml` — dependencia `spring-boot-starter-aspectj`
- `src/main/java/com/tecsup/model/AuditoriaLog.java` — nueva
- `src/main/java/com/tecsup/repository/AuditoriaRepository.java` — nueva
- `src/main/java/com/tecsup/service/AuditoriaService.java` — nueva
- `src/main/java/com/tecsup/service/ProductoService.java` — `actualizar(Long, Producto)`
- `src/main/java/com/tecsup/aspect/LoggingAspect.java` — nueva
- `src/main/java/com/tecsup/aspect/ErrorAspect.java` — nueva (errores a BD)
- `src/main/java/com/tecsup/aspect/AuditoriaAspect.java` — nueva
- `src/main/java/com/tecsup/controller/ProductoController.java` — PUT usa `service.actualizar`
- `scripts/laboratorio.sql` — DDL de `auditoria_log`
- `src/test/java/com/tecsup/AuditoriaAspectTest.java` — pruebas CRUD + ERROR