# ShopSphere – Order Service

Order Service is responsible for managing customer orders in the ShopSphere microservices-based e-commerce application.

It coordinates with Product Service to retrieve product details, Inventory Service to reserve stock, and RabbitMQ to publish order events for asynchronous processing by Notification Service.

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring REST Client
- RabbitMQ
- Maven
- Docker

## Architecture and Integrations

Order Service communicates with other services as follows:

- **Product Service:** Retrieves product details and pricing through REST APIs.
- **Inventory Service:** Reserves stock before an order is created and releases stock if order persistence fails.
- **RabbitMQ:** Publishes order-created events.
- **Notification Service:** Consumes order events asynchronously from RabbitMQ.

## Order Creation Flow

1. Receives an order request containing user ID, product ID and quantity.
2. Fetches product details from Product Service.
3. Requests Inventory Service to reserve the required stock.
4. Calculates the total order amount.
5. Saves the order with `PENDING` status in PostgreSQL.
6. Publishes an order-created event to RabbitMQ.
7. Notification Service consumes the event asynchronously.

If order persistence fails after inventory reservation, Order Service attempts to release the reserved stock.

## Order Status

- `PENDING`
- `CONFIRMED`
- `CANCELLED`

## REST APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders/{orderId}` | Retrieve an order by ID |
| GET | `/api/orders` | Retrieve all orders |

### Sample Create Order Request

```json
{
  "userId": 101,
  "productId": 3,
  "quantity": 2
}
```

## Database

PostgreSQL database: `shopsphere_order`

Orders are stored in the `customer_orders` table.

## Running Locally

Prerequisites:
- Java 17
- Maven
- PostgreSQL
- RabbitMQ
- Product Service and Inventory Service running

Configure the database and RabbitMQ connection properties in `application.properties`.

Run the application:

```bash
./mvnw spring-boot:run
```

The service runs on port `8083` by default.

## Docker

ShopSphere uses Docker Compose to run the services and their dependencies together.

The central Compose configuration is maintained in the ShopSphere project folder.

## CI Pipeline

GitHub Actions automatically builds and verifies the service on pushes and pull requests to the `main` or `master` branch.

The workflow runs Maven verification and builds the Docker image.

## Project Repository

[ShopSphere Order Service](https://github.com/shalini3033/shopsphere-order-service)
