# Kaizer-Back — Documentación técnica

API REST del e-commerce **Kaizer Tech**. Gestiona autenticación, catálogo de
productos, carrito/checkout con control de stock, pedidos, perfil de usuario y
consultas de identidad (DNI / RUC) para el mercado peruano.

---

## 1. Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 21 (LTS) |
| Framework | Spring Boot | 4.0.6 |
| Seguridad | Spring Security + JWT (jjwt) | 0.12.6 |
| Persistencia | Spring Data JPA / Hibernate | (gestionado por Boot) |
| Base de datos | PostgreSQL (Supabase) | 15+ |
| Pool de conexiones | HikariCP | (gestionado por Boot) |
| Build | Maven (wrapper `mvnw`) | 3.14.1 |
| Contenedor | Docker (multi-stage, Temurin Alpine) | JDK/JRE 21 |
| Integración externa | Decolecta API (RENIEC/SUNAT) | REST |

> **Nota de entorno local:** Maven usa `JAVA_HOME`. En esta máquina apunta a un
> JDK 17 (instalado por Metals). Para compilar hay que fijarlo al JDK 21:
> `set JAVA_HOME=C:\Program Files\Java\jdk-21.0.11` (o en PowerShell
> `$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"`).

---

## 2. Arquitectura

Organización **por feature** (vertical slicing). Cada paquete agrupa su
controller, service, repository, entidad y DTOs:

```
com.example.Kaizer_Back
├── KaizerBackApplication.java      → punto de entrada (@SpringBootApplication)
├── auth/                           → login, registro, emisión y validación de JWT
│   ├── AuthController              → POST /register, /login
│   ├── JwtService                  → genera/valida tokens, incrusta claims
│   ├── JwtAuthenticationFilter     → filtro por request que autentica vía Bearer
│   ├── UsuarioDetailsService       → carga UserDetails desde BD
│   ├── UsuarioPrincipal            → UserDetails + id/nombre para evitar queries
│   └── dto/ (AuthRequest, AuthResponse)
├── checkout/                       → creación de pedido con descuento de stock
│   ├── CheckoutController          → POST /api/checkout (público, principal opcional)
│   ├── CheckoutService             → lógica transaccional con bloqueo pesimista
│   ├── ApiExceptionHandler         → @RestControllerAdvice (manejo de errores)
│   ├── StockInsuficienteException
│   └── dto/ (CheckoutRequest, CheckoutResponse)
├── producto/                       → catálogo + pedidos
│   ├── Producto / ProductoController / ProductoService / ProductoRepository
│   ├── Pedido / PedidoItem / PedidoController / PedidoService / PedidoRepository
│   └── dto/ (ProductoRequest, ProductoResponse, PedidoResponse, PedidoItemResponse, EstadoRequest)
├── usuario/                        → perfil y roles
│   ├── Usuario / Role (USER, ADMIN)
│   ├── UsuarioController           → GET/PUT /api/usuarios/perfil
│   └── UsuarioService / UsuarioRepository / dto
├── integration/                    → consultas de identidad (Decolecta)
│   ├── DecolectaConfig             → RestClient compartido con token
│   ├── dni/ (RENIEC) → GET /api/consulta/dni/{dni}
│   └── ruc/ (SUNAT)  → GET /api/consulta/ruc/{ruc}
├── config/
│   ├── SecurityConfig              → filter chain, reglas de autorización, BCrypt
│   └── CorsConfig                  → orígenes permitidos (configurable por env)
└── health/
    └── HealthController            → GET /api/health (keep-alive / warm-up)
```

---

## 3. Modelo de datos

Esquema en `sql/kaizer_supabase.sql`. Cuatro tablas principales:

```
usuarios ──1:N── pedidos ──1:N── pedido_items ──N:1── productos
```

- **usuarios**: `id`, `email` (único), `password_hash` (BCrypt), `nombre`,
  `telefono`, `direccion`, `ciudad`, `role` (`USER`/`ADMIN`), `created_at`.
- **productos**: `id`, `nombre`, `descripcion`, `precio` (>0), `image_url`,
  `stock` (>=0), `created_at`, `updated_at`. Índice por `nombre`.
- **pedidos**: `id`, `usuario_id` (FK, `on delete set null`), `direccion_envio`,
  `nombre_comprador`, `telefono_comprador`, `estado` (default `CREADO`),
  `total` (>=0), `created_at`. Índice por `usuario_id`.
- **pedido_items**: `id`, `pedido_id` (FK cascade), `producto_id` (FK restrict),
  `cantidad` (>0), `precio_unitario` (>0), `subtotal` **columna generada**
  (`cantidad * precio_unitario`).

**Snapshot de facturación:** el pedido guarda `nombre_comprador`,
`telefono_comprador` y `direccion_envio` en el momento de la compra, de modo que
cambios posteriores en el perfil no alteran pedidos históricos.

> `spring.jpa.hibernate.ddl-auto=validate`: Hibernate **no crea ni modifica**
> tablas. El esquema se aplica manualmente en Supabase con los scripts SQL. Si el
> modelo Java y la BD divergen, la app **no arranca** (validación estricta).

---

## 4. Seguridad y autenticación

### Flujo JWT (stateless)

1. `POST /api/auth/register` o `/login` → valida credenciales con
   `AuthenticationManager` + `BCryptPasswordEncoder`.
2. `JwtService.generateToken()` emite un JWT firmado con **HMAC-SHA** que incrusta
   claims: `sub` (email), `uid` (id), `nombre`, `role`, `iat`, `exp`.
   Incrustar estos datos evita consultas redundantes a la BD.
3. El cliente envía `Authorization: Bearer <token>` en cada petición.
4. `JwtAuthenticationFilter` (un `OncePerRequestFilter`) valida la firma y
   expiración, carga el `UsuarioPrincipal` y puebla el `SecurityContext`.
   Si el token es inválido/ausente, la request continúa **sin** autenticar
   (los endpoints públicos siguen funcionando).

### Reglas de autorización (`SecurityConfig`)

| Regla | Acceso |
|---|---|
| `OPTIONS /**` | Público (preflight CORS) |
| `GET /api/health` | Público |
| `/api/auth/**` | Público |
| `GET /api/productos/**` | Público |
| `GET /api/consulta/dni/**`, `/ruc/**` | Público (autocompletado) |
| `POST /api/checkout` | Autenticado |
| `POST /api/productos/**` | Rol `ADMIN` |
| `GET`/`PATCH /api/pedidos/admin/**` | Rol `ADMIN` |
| Cualquier otra | Autenticado |

- Sesiones **STATELESS**, CSRF deshabilitado (API sin cookies de sesión).
- CORS gestionado por `CorsConfig` con orígenes configurables por variable de
  entorno, soportando comodines (`https://*.vercel.app`).

---

## 5. Referencia de endpoints

Base URL en producción: `https://kaizer-back-1.onrender.com`

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Registro; devuelve `{ token }` |
| POST | `/api/auth/login` | Público | Login; devuelve `{ token }` |
| GET | `/api/health` | Público | Health check / warm-up |
| GET | `/api/productos` | Público | Lista de productos |
| GET | `/api/productos/{id}` | Público | Detalle de producto |
| POST | `/api/productos` | ADMIN | Crear producto |
| POST | `/api/checkout` | Autenticado | Crear pedido y descontar stock |
| GET | `/api/usuarios/perfil` | Autenticado | Datos del perfil |
| PUT | `/api/usuarios/perfil` | Autenticado | Actualizar perfil |
| GET | `/api/pedidos/mis-pedidos` | Autenticado | Pedidos del usuario |
| GET | `/api/pedidos/admin/todos` | ADMIN | Todos los pedidos |
| PATCH | `/api/pedidos/admin/{id}/estado` | ADMIN | Cambiar estado del pedido |
| GET | `/api/consulta/dni/{dni}` | Público | Consulta RENIEC (Decolecta) |
| GET | `/api/consulta/ruc/{ruc}` | Público | Consulta SUNAT (Decolecta) |

---

## 6. Lógica de negocio destacada

### Checkout con control de concurrencia
`CheckoutService.checkout()` es `@Transactional(rollbackFor = Exception.class)`:

- Usa **bloqueo pesimista** (`findByIdForUpdate` con
  `LockModeType.PESSIMISTIC_WRITE`, es decir `SELECT ... FOR UPDATE`) para leer y
  descontar stock. Esto evita **race conditions** cuando dos compras compiten por
  el mismo inventario.
- Si `stock < cantidad` lanza `StockInsuficienteException` → HTTP **400** con
  mensaje legible (el frontend lo mapea a `StockInsuficienteError`).
- El endpoint es público pero el `principal` puede ser `null`: soporta tanto
  compra autenticada (asocia `usuario_id` + snapshot de perfil) como invitado
  (solo `direccion_envio`).
- El total se calcula en el servidor con `BigDecimal` (nunca se confía en montos
  del cliente).

### Integración Decolecta (DNI/RUC)
`DecolectaConfig` expone un `RestClient` compartido con la base URL y el token
`Authorization: Bearer <DECOLECTA_TOKEN>`. Se usa para autocompletar nombre/razón
social en el registro y checkout. **Requiere un token válido** (`DECOLECTA_TOKEN`)
para funcionar; sin él estas consultas fallan.

---

## 7. Configuración (variables de entorno)

Definidas en `src/main/resources/application.properties` con valores por defecto.
Todas se pueden sobreescribir por entorno. Soporta `.env` opcional
(`spring.config.import=optional:dotenv:.env`).

| Variable | Obligatoria | Default | Descripción |
|---|---|---|---|
| `PORT` | No | `9090` | Puerto HTTP (Render lo inyecta) |
| `SPRING_DATASOURCE_URL` / `DATABASE_URL` | **Sí** | — | JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` / `DATABASE_USERNAME` | **Sí** | — | Usuario BD |
| `SPRING_DATASOURCE_PASSWORD` / `DATABASE_PASSWORD` | **Sí** | — | Password BD |
| `JWT_SECRET` | **Sí (prod)** | secreto de dev | Clave HMAC del JWT (≥ 32 bytes) |
| `JWT_EXP_MINUTES` | No | `1440` (24 h) | Expiración del token |
| `CORS_ALLOWED_ORIGINS` | Recomendada | localhost + vercel | CSV de orígenes; admite `*` |
| `DECOLECTA_URL` | No | `https://api.decolecta.com` | Base API identidad |
| `DECOLECTA_TOKEN` | Para DNI/RUC | vacío | Token de Decolecta |
| `JPA_DDL_AUTO` | No | `validate` | Estrategia DDL de Hibernate |
| `JPA_SHOW_SQL` | No | `false` | Log de SQL |
| `DB_POOL_MAX` | No | `5` | Tamaño máx. del pool Hikari |
| `DB_POOL_MIN_IDLE` | No | `1` | Conexiones idle mínimas |

> **Importante para producción:** define siempre un `JWT_SECRET` propio y largo.
> El default (`kaizer-tech-dev-only-...`) es solo para desarrollo; dejarlo en prod
> permitiría a cualquiera falsificar tokens.

---

## 8. Despliegue en Render

El backend ya está desplegado en `https://kaizer-back-1.onrender.com`. El
despliegue usa el **Dockerfile** multi-stage (build con JDK 21 → runtime con JRE
21 Alpine), que respeta la variable `PORT` de Render.

### Pasos para (re)crear el servicio

1. **Base de datos primero** (ver sección 9). Necesitas la connection string.

2. En Render → **New → Web Service** → conecta el repositorio del backend.

3. Configuración del servicio:
   - **Runtime:** `Docker` (Render detecta el `Dockerfile` en `Kaizer-Back/`).
   - **Root Directory:** `Kaizer-Back` (si el repo contiene también el frontend).
   - **Instance Type:** Free o superior. *(En el plan Free el servicio se duerme
     tras inactividad; ver nota de warm-up abajo.)*
   - **Health Check Path:** `/api/health`.

4. **Environment Variables** (Render → Environment). Mínimo:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=<usuario>
   SPRING_DATASOURCE_PASSWORD=<password>
   JWT_SECRET=<cadena-secreta-larga-y-aleatoria>
   CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app,https://*.vercel.app
   DECOLECTA_TOKEN=<token-decolecta>
   ```
   No necesitas definir `PORT`: Render lo inyecta y el `ENTRYPOINT` lo usa.

5. **Deploy.** Render construye la imagen y publica. Verifica en logs que
   Hibernate valide el esquema sin errores y que arranque en el puerto de Render.

6. **Verificación:** `GET https://<tu-servicio>.onrender.com/api/health` debe
   responder 200.

### Nota sobre el plan Free (cold start)
En el plan gratuito el servicio se suspende tras ~15 min de inactividad y la
primera petición tarda en despertar. Por eso el frontend expone
`BackendService.warmUp()` que llama a `/api/health` de forma silenciosa para
"calentar" el servidor. Para eliminar el cold start por completo, usa un plan de
pago o un cron externo que haga ping a `/api/health`.

### Conexión BD ↔ pool (Supabase)
Supabase limita conexiones. El pool Hikari está configurado conservador
(`DB_POOL_MAX=5`). Si usas el **connection pooler** de Supabase (puerto `6543`,
modo transaction), ajusta la URL en consecuencia. No subas el pool sin verificar
el límite de tu plan de Supabase.

---

## 9. Base de datos (Supabase)

Ejecuta en el **SQL Editor** de Supabase, en orden:

1. **`sql/kaizer_supabase.sql`** — crea extensión `pgcrypto`, las 4 tablas,
   índices y un usuario **admin semilla**:
   - email: `admin@kaizer.tech`
   - password: `Admin12345!`  ← **cámbialo tras el primer login en producción.**

2. **`sql/rls_productos.sql`** — habilita **Row Level Security** sobre
   `productos`: lectura pública/autenticada (`SELECT`), pero **sin** insert/update/
   delete para roles anon/authenticated (solo el backend con service role escribe),
   y añade la tabla a la **publicación realtime** de Supabase.

> El frontend Angular lee el catálogo **directamente desde Supabase** (con la
> anon key + realtime) para tener actualizaciones en vivo, mientras que las
> escrituras (crear producto, checkout, pedidos) pasan por este backend. Por eso
> RLS permite `SELECT` público pero bloquea escrituras desde el cliente.

---

## 10. Desarrollo local

```bash
# 1. Fijar JDK 21
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"   # PowerShell

# 2. (Opcional) crear un .env en Kaizer-Back/ con las variables de la sección 7

# 3. Compilar
./mvnw clean compile

# 4. Ejecutar tests (16 tests: checkout, DNI, RUC, exception handler, context)
./mvnw test

# 5. Levantar la app (por defecto en el puerto 9090)
./mvnw spring-boot:run
```

Sin base de datos configurada, la app no arrancará (necesita datasource válido y
`ddl-auto=validate` requiere que las tablas existan).

---

## 11. Pruebas

16 tests unitarios/integración, todos en verde:

- `CheckoutServiceTest` — descuento de stock y stock insuficiente.
- `ApiExceptionHandlerTest` — mapeo de errores a respuestas HTTP.
- `DniServiceTest` / `RucServiceTest` — parseo de respuestas Decolecta.
- `KaizerBackApplicationTests` — carga del contexto Spring.

Los tests usan `src/test/resources/application-test.properties` (perfil aislado,
sin BD real).

---

## 12. Recomendaciones / mejoras futuras

- **Rotar el `JWT_SECRET`** y cambiar la contraseña admin semilla en producción.
- **Documentación viva de la API:** añadir `springdoc-openapi` para exponer
  Swagger UI en `/swagger-ui.html`.
- **Refresh tokens:** hoy solo hay access token de 24 h; considerar rotación.
- **Rate limiting** en `/api/auth/**` y en las consultas DNI/RUC (evita abuso del
  token de Decolecta, que suele ser de pago por consulta).
- **Estados de pedido tipados:** `estado` es un `String` libre; convendría un enum
  validado (`CREADO`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`).
- **Observabilidad:** habilitar `spring-boot-starter-actuator` para métricas y un
  health check más rico.
