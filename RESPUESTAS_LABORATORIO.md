# Laboratorio 05 - Validación y Manejo de Errores

**Proyecto:** `productos-api` — API REST de Productos
**Stack:** Spring Boot 4.1.1 · Java · Spring Data JPA · MySQL · Bean Validation
**Paquete base:** `com.tecsup`

---

## 1. Estructura del proyecto

```
productos-api/
├── pom.xml
├── mvnw / mvnw.cmd
├── scripts/laboratorio.sql
└── src
    ├── main
    │   ├── java/com/tecsup
    │   │   ├── ProductosApiApplication.java
    │   │   ├── controller/ProductoController.java
    │   │   ├── dto/ProductoDTO.java
    │   │   ├── exception/GlobalExceptionHandler.java
    │   │   ├── model/Producto.java
    │   │   ├── repository/ProductoRepository.java
    │   │   └── service/ProductoService.java
    │   └── resources/application.properties
    └── test
        ├── java/com/tecsup/ProductoControllerTest.java
        └── resources/application.properties   (H2 en memoria)
```

## 2. Base de datos

```sql
CREATE DATABASE IF NOT EXISTS laboratorio_api;
USE laboratorio_api;
```

La tabla `producto` la crea Hibernate automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

## 3. Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/productos` | Lista todos los productos |
| POST | `/api/productos` | Crea un producto (valida el DTO) |
| GET | `/api/productos/{id}` | Obtiene un producto por ID (404 si no existe) |
| GET | `/api/productos/buscar?nombre=texto` | **Actividad:** busca productos por nombre |
| PUT | `/api/productos/{id}` | Actualiza un producto |
| DELETE | `/api/productos/{id}` | Elimina un producto |

### Ejemplo POST (incluye el nuevo campo `categoria`)

```json
{
  "nombre": "Monitor",
  "precio": 50,
  "stock": 20,
  "categoria": "Perifericos"
}
```

### Respuesta de error de validación (HTTP 400)

```json
{
  "nombre": "El nombre es obligatorio",
  "precio": "El precio debe ser mayor a 0",
  "stock": "El stock no puede ser negativo",
  "categoria": "La categoria es obligatoria"
}
```

---

## Actividad

Se agregó el campo `private String categoria;` en la entidad `Producto` y en `ProductoDTO`
(con `@NotBlank`), se actualizó el mapeo en POST y PUT, y se implementó el endpoint de
búsqueda por nombre:

- `ProductoRepository`: `List<Producto> findByNombreContainingIgnoreCase(String nombre)`
- `ProductoService`: `buscarPorNombre(String nombre)`
- `ProductoController`: `GET /api/productos/buscar?nombre=...`

---

## Preguntas

**1. ¿Por qué usamos un DTO en lugar de exponer directamente la entidad?**

Porque el DTO separa el contrato de la API del modelo de persistencia. Evita exponer
directamente la entidad (y con ella campos internos o relaciones que no queremos publicar),
permite aplicar validaciones de entrada específicas (`@NotBlank`, `@Positive`, etc.) sin
ensuciar el modelo, protege la API ante cambios en la base de datos y evita problemas de
serialización (por ejemplo, enviar el `id` en un POST o generar bucles con relaciones
bidireccionales). En resumen: desacoplamiento, seguridad y control del contrato.

**2. ¿Qué función cumple `@Valid`?**

Activa la validación de Jakarta Bean Validation sobre el objeto anotado. Cuando se usa en un
`@RequestBody`, Spring evalúa los constraints del DTO (`@NotBlank`, `@Positive`, `@Min`, etc.)
**antes** de ejecutar el método del controlador. Si alguna regla falla, se lanza
`MethodArgumentNotValidException` y el flujo pasa al `GlobalExceptionHandler`, que responde 400
con los mensajes por campo.

**3. ¿Qué pasaría si eliminamos `@RestControllerAdvice`?**

Las excepciones de validación dejarían de capturarse de forma centralizada. Spring devolvería su
respuesta de error por defecto (400 con el JSON estándar de error del framework) en lugar del
`Map<String, String>` personalizado con un mensaje claro por cada campo. Se perdería el manejo
homogéneo y legible de errores en toda la API.

**4. ¿Qué hace `@Autowired`?**

Indica al contenedor de Spring que inyecte una instancia del bean requerido (por ejemplo
`ProductoRepository` o `ProductoService`) mediante inyección de dependencias. Así el controlador
y el servicio obtienen los objetos ya gestionados por Spring, sin instanciarlos manualmente con
`new`, lo que facilita el desacoplamiento y las pruebas.

**5. ¿Cuál es la diferencia entre `save()`, `findById()` y `findAll()`?**

- **`save(entidad)`**: inserta un registro nuevo (si el `id` es nulo) o actualiza uno existente.
  Devuelve la entidad persistida, ya con el `id` generado.
- **`findById(id)`**: busca por clave primaria. Devuelve un `Optional<T>`, que estará vacío si el
  registro no existe (en el laboratorio se usa `.orElse(null)`).
- **`findAll()`**: devuelve una `List<T>` con todos los registros de la tabla.

---

## OBSERVACIONES

1. La clave del root en XAMPP viene vacía, no es "root"; con la clave mal puesta la app no
   arrancaba y daba "Access denied".

2. Cuando ya tenía una instancia abierta, el puerto 8080 se ocupaba y el `spring-boot:run` fallaba;
   tuve que cerrar el proceso anterior antes de volver a iniciar.

3. Al agregar `categoria` tuve que tocarla en la entidad, en el DTO y en el mapeo del POST/PUT;
   si me olvidaba del mapeo, se guardaba como NULL aunque la validación pasara.

4. El endpoint `/buscar` choca con `/{id}` porque "buscar" se toma como un id; hay que declararlo
   aparte para que funcione.

5. Aparece el aviso de "open-in-view", pero no es error: es solo el comportamiento por defecto de
   Spring Boot con JPA.

## CONCLUSIONES

1. Usar DTO + `@Valid` + manejador de errores hizo que las respuestas de validación salieran
   claras y ordenadas (400 con un mensaje por campo).

2. Separar la entidad del DTO mantiene la API más limpia y protege el modelo de la base de datos.

3. El `@RestControllerAdvice` me ahorró repetir `try/catch` y dejó los errores con el mismo formato.

4. Buscar por nombre fue muy fácil con Spring Data JPA (`findByNombreContainingIgnoreCase`), sin
   escribir SQL.

5. Probando la API de verdad y con tests encontré los problemas de clave y puerto, y confirmé que
   cambiar de versión de Spring trae sorpresas.

---

## Cómo ejecutar

Entorno usado: **XAMPP (MariaDB 10.4)**, usuario `root` **sin contraseña** (configuración por
defecto de XAMPP). En `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/laboratorio_api?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
```

```powershell
# 1) Encender MySQL/MariaDB desde el Panel de Control de XAMPP

# 2) (Opcional) crear la base de datos; tambien se crea automaticamente
#    por createDatabaseIfNotExist=true. Tambien disponible scripts/laboratorio.sql
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS laboratorio_api;"

# 3) Ejecutar la aplicacion
.\mvnw.cmd spring-boot:run

# 4) Ejecutar las pruebas (usan H2 en memoria, no requieren MySQL)
.\mvnw.cmd test
```

> Si tu MySQL tiene contraseña, colócala en `spring.datasource.password`.
