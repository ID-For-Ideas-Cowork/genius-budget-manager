# Budget Manager

API REST para gestión de presupuestos de campañas de marketing digital. Proyecto interno de Genius Agency.

## Contexto de negocio

Genius es una agencia de marketing digital distribuida que gestiona campañas para clientes en toda LATAM. Una parte central de la operación es el control de presupuestos: cada campaña tiene un presupuesto asignado, gastos registrados durante su ejecución y un estado que varía a lo largo del tiempo.

**Cliente de referencia: SueñoSimple**

SueñoSimple es una empresa de colchones con operación en Argentina. Su modelo de negocio depende de picos de demanda (temporada de verano, Hot Sale, Black Friday) y terceriza sus campañas de performance a Genius durante esos períodos. El equipo de Genius gestiona simultáneamente varias campañas para SueñoSimple que incluyen Meta Ads, email marketing, social ads e influencers.

Esta API resuelve la necesidad de tener visibilidad centralizada sobre el estado financiero de cada campaña: cuánto se asignó, cuánto se consumió y cuánto queda disponible.

## Qué resuelve cada endpoint

| Endpoint | Problema que resuelve en Genius / SueñoSimple |
|----------|-----------------------------------------------|
| `GET /api/campaigns` | El equipo necesita ver el listado completo de campañas activas y pasadas sin tener que consultar a otra persona. |
| `GET /api/campaigns?status=active` | El account manager de SueñoSimple quiere ver solo las campañas en curso para reportar el estado semanal. |
| `GET /api/campaigns/{id}` | Un developer o media buyer necesita el detalle de una campaña específica antes de registrar un gasto. |
| `GET /api/campaigns/{id}/summary` | El cliente pide saber cuánto presupuesto consumió hasta hoy y cuánto le queda disponible. |
| `GET /api/campaigns/{id}/expenses` | El equipo de administración necesita auditar todos los gastos de una campaña para validar la facturación. |
| `POST /api/campaigns/{id}/expenses` | Cada vez que se realiza una compra de medios (Meta Ads, TikTok, influencer), el equipo registra el gasto en tiempo real. |
| `PUT /api/campaigns/{id}/budget` | Durante una campaña puede ocurrir un refuerzo de presupuesto. El equipo lo actualiza sin tener que reiniciar la campaña. |

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación inicia en `http://localhost:8080`.

## Cómo probar la API sin frontend

Este proyecto incluye Swagger UI, una interfaz visual que permite ejecutar cualquier endpoint directamente desde el navegador sin necesidad de una aplicación frontend ni de instalar herramientas adicionales.

**Acceso:** `http://localhost:8080/swagger-ui.html` (con la aplicación corriendo)

### Pasos para probar un endpoint en Swagger

1. Abrir `http://localhost:8080/swagger-ui.html` en el navegador.
2. Se muestra la lista de endpoints agrupados. Hacer clic sobre el endpoint a probar para expandirlo.
3. Hacer clic en el botón **Try it out** (esquina superior derecha del endpoint).
4. Completar los parámetros requeridos:
   - Si el endpoint tiene un **path param** (como `{id}`), escribir el valor en el campo correspondiente.
   - Si el endpoint acepta un **query param** (como `?status=active`), completar el campo que aparece.
   - Si el endpoint requiere un **body** (POST o PUT), el campo de texto ya muestra un ejemplo del JSON esperado. Editarlo con los valores deseados.
5. Hacer clic en **Execute**.
6. La respuesta aparece debajo: código HTTP, headers y body en formato JSON.

### Ejemplo: registrar un gasto en la campaña 3

1. Expandir `POST /api/campaigns/{id}/expenses`.
2. Hacer clic en **Try it out**.
3. En el campo `id`, ingresar `3`.
4. En el body, ingresar:
```json
{
  "description": "Meta Ads Abril",
  "amount": 18000.0,
  "category": "ads_spend",
  "date": "2026-04-10"
}
```
5. Hacer clic en **Execute**. La respuesta debe ser `201 Created` con el gasto registrado.

### Alternativa: Postman

La colección también puede importarse en Postman usando la especificación OpenAPI disponible en `http://localhost:8080/api-docs`.

## Endpoints disponibles

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/campaigns` | Listar todas las campañas |
| GET | `/api/campaigns?status={status}` | Filtrar campañas por estado |
| GET | `/api/campaigns/{id}` | Obtener campaña por ID |
| GET | `/api/campaigns/{id}/summary` | Resumen de presupuesto |
| GET | `/api/campaigns/{id}/expenses` | Listar gastos de la campaña |
| POST | `/api/campaigns/{id}/expenses` | Registrar un gasto |
| PUT | `/api/campaigns/{id}/budget` | Actualizar presupuesto |

Los valores válidos para `status` son: `active`, `paused`, `closed`, `draft`.

Los valores válidos para `category` en gastos son: `ads_spend`, `creative`, `tools`, `agency_fee`.

## Datos de prueba

El sistema carga datos en memoria al iniciar. No requiere base de datos ni migraciones.

Campañas disponibles:

| ID | Nombre | Cliente | Estado |
|----|--------|---------|--------|
| 1 | Black Friday 2025 - Display | SuenoSimple | closed |
| 2 | Email Recupero de Carritos | SuenoSimple | active |
| 3 | Social Ads Q1 2026 | SuenoSimple | active |
| 4 | Influencers Verano 2026 | SuenoSimple | paused |
| 5 | Google Ads Performance - Marzo | TechStore | active |
| 6 | Branding Digital Q2 2026 | TechStore | draft |

## Estructura del proyecto

```
src/main/java/com/genius/budgetmanager/
├── controller/     Endpoints HTTP
├── service/        Lógica de negocio
├── model/          Entidades y DTOs
├── repository/     Datos en memoria
└── exception/      Manejo global de errores
```

## Cómo trabajar en este repositorio

**Con cuenta de GitHub:** hacer un fork del repositorio y clonar tu fork para trabajar en tu propia copia. No realizar commits directamente sobre la rama principal del repositorio original.

**Sin cuenta de GitHub:** descargar el proyecto como ZIP desde el botón "Code → Download ZIP" del repositorio y extraerlo localmente.
