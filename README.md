# Kaizer-Back

**Backend API REST** del e-commerce **Kaizer Tech**. Gestiona autenticación JWT, catálogo de productos, carrito/checkout con control de stock, pedidos, perfil de usuario y consultas de identidad (DNI / RUC) para el mercado peruano.

## 📌 Stack tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Framework:** Spring Boot 4.0.6
- **Seguridad:** Spring Security + JWT (jjwt 0.12.6)
- **Persistencia:** Spring Data JPA / Hibernate
- **Base de datos:** PostgreSQL 15+ (Supabase)
- **Pool conexiones:** HikariCP
- **Build:** Maven 3.14.1 (wrapper `mvnw`)
- **Contenedor:** Docker (multi-stage, Temurin Alpine JDK/JRE 21)
- **Integración externa:** Decolecta API (RENIEC/SUNAT)
- **Tests:** JUnit 5, Mockito (16 tests unitarios/integración)

## 🚀 Quick Start

### Requisitos

- **Java 21** (LTS)
- **Maven 3.8+** (incluido como wrapper `mvnw`)
- **PostgreSQL 15+** o **Supabase** (base de datos)
- **Git**

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/AndreeQuispe12/Kaizer-Back.git
cd Kaizer-Back

# 2. Configurar base de datos (ver sección "Base de datos")
# Ejecutar sql/kaizer_supabase.sql y sql/rls_productos.sql en Supabase

# 3. Crear .env en la raíz (opcional, ver .env.example)
# Las variables también se pueden inyectar por entorno
```

### Compilar

```bash
# En Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
.\mvnw clean compile

# En Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./mvnw clean compile
```

### Ejecutar tests

```bash
# Todos los tests (16/16 deben pasar)
.\mvnw test

# Con cobertura
.\mvnw test jacoco:report
```

### Ejecutar la aplicación

```bash
# Desarrollo local (puerto 9090 por defecto)
.\mvnw spring-boot:run

# Compilar JAR
.\mvnw clean package

# Ejecutar JAR
java -jar target/Kaizer-Back-0.0.1-SNAPSHOT.jar
```

---

## 📂 Estructura del proyecto

```
src/
├── main/java/com/example/Kaizer_Back/
│   ├── KaizerBackApplication.java          → Punto de entrada (@SpringBootApplication)
│   ├── auth/                               → Autenticación JWT
│   │   ├── AuthController                  → POST /api/auth/login, /register
│   │   ├── JwtService                      → Genera/valida tokens JWT
│   │   ├── JwtAuthenticationFilter         → Filtro por request (Bearer token)
│   │   ├── UsuarioDetailsService           → Carga UserDetails desde BD
│   │   ├── UsuarioPrincipal                → UserDetails + id/nombre
│   │   └── dto/                            → AuthRequest, AuthResponse
│   ├── checkout/                           → Checkout y pedidos
│   │   ├── CheckoutController              → POST /api/checkout
│   │   ├── CheckoutService                 → Lógica transaccional (bloqueo pesimista)
│   │   ├── ApiExceptionHandler             → Mapeo de errores a HTTP
│   │   ├── StockInsuficienteException
│   │   └── dto/                            → CheckoutRequest, CheckoutResponse
│   ├── producto/                           → Catálogo y pedidos
│   │   ├── Producto / ProductoController / ProductoService / ProductoRepository
│   │   ├── Pedido / PedidoItem / PedidoController / PedidoService / PedidoRepository
│   │   └── dto/                            → ProductoRequest, PedidoResponse, etc.
│   ├── usuario/                            → Perfil y roles
│   │   ├── Usuario / Role (USER, ADMIN)
│   │   ├── UsuarioController               → GET/PUT /api/usuarios/perfil
│   │   ├── UsuarioService / UsuarioRepository
│   │   └── dto/                            → UsuarioProfileRequest, UsuarioProfileResponse
│   ├── integration/                        → Consultas de identidad
│   │   ├── DecolectaConfig                 → RestClient compartido
│   │   ├── dni/ (RENIEC)                   → GET /api/consulta/dni/{dni}
│   │   └── ruc/ (SUNAT)                    → GET /api/consulta/ruc/{ruc}
│   ├── config/
│   │   ├── SecurityConfig                  → Filter chain, autorización, BCrypt
│   │   └── CorsConfig                      → Orígenes permitidos (configurable)
│   └── health/
│       └── HealthController                → GET /api/health (keep-alive)
├── test/java/com/example/Kaizer_Back/     → Tests unitarios/integración (16 tests)
├── resources/
│   ├── application.properties               → Configuración
│   └── application-test.properties          → Configuración para tests
└── sql/
    ├── kaizer_supabase.sql                 → Schema + datos semilla
    └── rls_productos.sql                   → Row Level Security
```

---

## 🔐 Autenticación

### Flujo JWT (stateless)

1. **Registro/Login:** `POST /api/auth/register` o `/api/auth/login`
   - Email + Password → valida con `AuthenticationManager` + `BCryptPasswordEncoder`
   
2. **Emisión de token:** `JwtService.generateToken()`
   - JWT firmado con **HMAC-SHA**
   - Claims: `sub` (email), `uid` (id), `nombre`, `role`, `iat`, `exp`
   - Incrusta datos para evitar consultas redundantes a BD
   
3. **Validación:** `JwtAuthenticationFilter` en cada request
   - Lee header `Authorization: Bearer <token>`
   - Valida firma y expiración
   - Carga `UsuarioPrincipal` al `SecurityContext`
   
4. **Autorización:** `SecurityConfig` define reglas por endpoint
   - Público: `/api/health`, `/api/auth/**`, `GET /api/productos/**`
   - Autenticado: `/api/checkout`, `/api/usuarios/perfil`, `/api/pedidos/mis-pedidos`
   - ADMIN: `POST /api/productos/**`, `GET/PATCH /api/pedidos/admin/**`

### Usuario admin semilla

Email: `admin@kaizer.tech`  
Password: `Admin12345!` (⚠️ **cámbialo tras el primer login en producción**)

---

## 📋 Endpoints API

Base URL: `https://kaizer-back-1.onrender.com`

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Registro; devuelve `{ token }` |
| POST | `/api/auth/login` | Público | Login; devuelve `{ token }` |
| GET | `/api/health` | Público | Health check / warm-up |
| GET | `/api/productos` | Público | Lista de productos |
| GET | `/api/productos/{id}` | Público | Detalle de producto |
| POST | `/api/productos` | ADMIN | Crear producto |
| POST | `/api/checkout` | Autenticado* | Crear pedido y descontar stock |
| GET | `/api/usuarios/perfil` | Autenticado | Datos del perfil |
| PUT | `/api/usuarios/perfil` | Autenticado | Actualizar perfil |
| GET | `/api/pedidos/mis-pedidos` | Autenticado | Pedidos del usuario |
| GET | `/api/pedidos/admin/todos` | ADMIN | Todos los pedidos |
| PATCH | `/api/pedidos/admin/{id}/estado` | ADMIN | Cambiar estado del pedido |
| GET | `/api/consulta/dni/{dni}` | Público | Consulta RENIEC (Decolecta) |
| GET | `/api/consulta/ruc/{ruc}` | Público | Consulta SUNAT (Decolecta) |

*`/api/checkout` acepta usuario anónimo, pero la protección del lado cliente la restringe.

---

## 🗂️ Base de datos (Supabase)

### Setup inicial

Ejecuta en el **SQL Editor** de Supabase en orden:

1. **`sql/kaizer_supabase.sql`**
   - Crea extensión `pgcrypto`
   - Crea 4 tablas: usuarios, productos, pedidos, pedido_items
   - Crea índices
   - Inserta usuario admin semilla

2. **`sql/rls_productos.sql`**
   - Habilita Row Level Security sobre `productos`
   - Lectura pública/autenticada (`SELECT`)
   - Bloquea insert/update/delete desde cliente (solo backend escribe)
   - Añade tabla a publicación realtime de Supabase

### Modelo de datos

```
usuarios ──1:N── pedidos ──1:N── pedido_items ──N:1── productos
```

**Tablas:**
- `usuarios`: id, email (único), password_hash (BCrypt), nombre, telefono, direccion, ciudad, role (USER/ADMIN), created_at
- `productos`: id, nombre, descripcion, precio (>0), image_url, stock (>=0), created_at, updated_at
- `pedidos`: id, usuario_id (FK), direccion_envio, nombre_comprador, telefono_comprador, estado (default CREADO), total (>=0), created_at
- `pedido_items`: id, pedido_id (FK cascade), producto_id (FK restrict), cantidad (>0), precio_unitario (>0), subtotal (generada)

**Snapshot de facturación:** El pedido guarda nombre_comprador, telefono_comprador y direccion_envio en el momento de la compra. Cambios posteriores en el perfil no afectan pedidos históricos.

---

## ⚙️ Configuración (variables de entorno)

Definidas en `src/main/resources/application.properties`. Se pueden sobreescribir por entorno.

| Variable | Obligatoria | Default | Descripción |
|---|---|---|---|
| `PORT` | No | `9090` | Puerto HTTP (Render lo inyecta) |
| `SPRING_DATASOURCE_URL` | **Sí** | — | JDBC de PostgreSQL (ej: `jdbc:postgresql://host:5432/postgres?sslmode=require`) |
| `SPRING_DATASOURCE_USERNAME` | **Sí** | — | Usuario BD |
| `SPRING_DATASOURCE_PASSWORD` | **Sí** | — | Password BD |
| `JWT_SECRET` | **Sí (prod)** | secreto dev | Clave HMAC del JWT (≥32 bytes) |
| `JWT_EXP_MINUTES` | No | `1440` (24 h) | Expiración del token |
| `CORS_ALLOWED_ORIGINS` | Recomendada | localhost + vercel | CSV de orígenes; admite `*` |
| `DECOLECTA_URL` | No | `https://api.decolecta.com` | Base API identidad |
| `DECOLECTA_TOKEN` | Para DNI/RUC | vacío | Token de Decolecta |
| `JPA_DDL_AUTO` | No | `validate` | Estrategia DDL de Hibernate |
| `DB_POOL_MAX` | No | `5` | Tamaño máx. del pool Hikari |

**⚠️ Importante:** En producción define siempre un `JWT_SECRET` propio y largo. El default es solo para desarrollo.

### Crear `.env` local (opcional)

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/kaizer?sslmode=disable
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu-password
JWT_SECRET=tu-clave-secreta-muy-larga-y-aleatoria
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5173
DECOLECTA_TOKEN=tu-token-decolecta
```

Spring Boot detecta automáticamente `.env` (configurado en `application.properties`).

---

## 🚀 Despliegue en Render

El backend ya está desplegado en `https://kaizer-back-1.onrender.com`.

### Pasos para (re)crear el servicio

1. **Base de datos primero:** Supabase con scripts SQL ejecutados (ver sección anterior)

2. **En Render:**
   - New → Web Service
   - Conecta tu repo de GitHub
   - Runtime: Docker (detecta `Dockerfile` en `Kaizer-Back/`)
   - Root Directory: `Kaizer-Back`
   - Instance Type: Free (o superior si quieres evitar cold start)
   - Health Check Path: `/api/health`

3. **Environment Variables** (Render → Environment):
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=<usuario>
   SPRING_DATASOURCE_PASSWORD=<password>
   JWT_SECRET=<cadena-secreta-larga-y-aleatoria>
   CORS_ALLOWED_ORIGINS=https://kaizer-front-xxxxx.vercel.app,https://*.vercel.app
   DECOLECTA_TOKEN=<token-decolecta>
   ```
   **No definas `PORT`:** Render lo inyecta automáticamente.

4. **Deploy:** Render construye la imagen y publica

5. **Verificación:** `GET https://<tu-servicio>.onrender.com/api/health` debe responder 200

### Docker

El `Dockerfile` usa multi-stage:
- **Build stage:** JDK 21 Alpine para compilar
- **Runtime stage:** JRE 21 Alpine (más pequeño)
- **ENTRYPOINT:** Respeta la variable `PORT` de Render

### Nota sobre plan Free

En el plan gratuito el servicio se suspende tras ~15 min de inactividad. La primera petición tarda en despertar (**cold start**). El frontend mitiga esto con `BackendService.warmUp()` que hace ping silencioso a `/api/health`.

---

## 🔍 Lógica de negocio destacada

### Checkout con control de concurrencia

`CheckoutService.checkout()` es `@Transactional(rollbackFor = Exception.class)`:

- Usa **bloqueo pesimista** (`findByIdForUpdate` con `LockModeType.PESSIMISTIC_WRITE`, es decir `SELECT ... FOR UPDATE`)
- Evita **race conditions** cuando dos compras compiten por el mismo stock
- Si `stock < cantidad` lanza `StockInsuficienteException` → HTTP 400
- Soporta compra autenticada (asocia usuario_id + snapshot de perfil) e invitado (solo dirección_envio)
- El total se calcula en servidor con `BigDecimal` (nunca se confía en montos del cliente)

### Integración Decolecta (DNI/RUC)

`DecolectaConfig` expone un `RestClient` compartido con token `Authorization: Bearer <DECOLECTA_TOKEN>`.

- DNI de 8 dígitos → consulta RENIEC (devuelve nombreCompleto)
- RUC → consulta SUNAT (devuelve razonSocial)
- **Requiere token válido** para funcionar

---

## ✅ Tests

16 tests unitarios/integración, todos en verde:

```
CheckoutServiceTest (2 tests)
  - Descuento de stock
  - Stock insuficiente → excepción

ApiExceptionHandlerTest (2 tests)
  - Mapeo de excepciones a respuestas HTTP

DniServiceTest (6 tests)
  - Parseo de respuestas RENIEC

RucServiceTest (5 tests)
  - Parseo de respuestas SUNAT

KaizerBackApplicationTests (1 test)
  - Carga del contexto Spring
```

Ejecuta con:
```bash
.\mvnw test
```

Los tests usan `src/test/resources/application-test.properties` (perfil aislado, sin BD real).

---

## 🤝 Contribuir

1. Fork el repo
2. Crea una rama (`git checkout -b feature/tu-feature`)
3. Commit cambios (`git commit -m 'Add tu-feature'`)
4. Push a la rama (`git push origin feature/tu-feature`)
5. Abre un Pull Request

---

## 📖 Documentación

- **DOCUMENTACION_BACKEND.docx** — Guía técnica completa (arquitectura, endpoints, despliegue, BD)
- **DOCUMENTACION_FRONTEND.docx** — Documentación del frontend (ver repo de Kaizer-Front-Angular)

---

## 📧 Contacto

- **Email:** andreequispe96@gmail.com
- **GitHub:** [AndreeQuispe12](https://github.com/AndreeQuispe12)

---

**Última actualización:** Julio 2026  
**Versión:** 1.0.0
