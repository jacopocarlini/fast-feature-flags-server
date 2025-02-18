# Fast Feature Flags Server

The **Fast Feature Flags Server** is an open-source server designed to centralize the management of **feature flags** and connect to a **MongoDB** database. This server is part of a broader project that follows the **OpenFeature** standard for efficient and scalable flag management in applications.

## Features

- **Centralized Feature Flags Management**: The server allows you to manage all feature flags through a single configuration.
- **MongoDB Integration**: It connects to MongoDB using a connection string to store and retrieve feature flags.
- **REST API**: Exposes CRUD operations for managing feature flags via a RESTful API.

## Requirements

- **Java 17+**
- **MongoDB 4.2+**
- **Spring Boot 3.x+**

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/jacopocarlini/fast-feature-flags-server.git
cd fast-feature-flags-server
```

### 2. Configure MongoDB Connection

Make sure MongoDB is installed and running on your machine or remote server. You can configure your MongoDB connection string in the `application.properties` file:

```properties
spring.data.mongodb.uri=mongodb://<username>:<password>@<host>:<port>/<database>
```

### 3. Run the Server

Once the project is built, you can start the server with the following command:

```bash
mvn clean spring-boot:run
```

The server will be available on port `8080` by default. You can change the port in the `application.properties` file if needed.

## API Endpoints

The server exposes several APIs for managing feature flags. These APIs allow you to create, read, update, and delete feature flags.

### Get all feature flags

```http
GET /flags
```

### Get a specific feature flag

```http
GET /flags/{id}
```

### Create a new feature flag

```http
POST /flags
```

Request Body:

```json
{
  "flagKey": "flagKey_790f8cac1df8",
  "enabled": false,
  "variants": [
    {
      "defaultVariant": false,
      "name": "name_c362910cf198",
      "value": {},
      "percentage": 0,
      "target": "target_c03a7322c14c"
    }
  ],
  "timeWindowStart": "2025-03-14 16:05:28",
  "timeWindowEnd": "2025-03-14 16:05:28"
}
```

### Update a feature flag

```http
PUT /flags/{id}
```

Request Body:

```json
{
  "flagKey": "flagKey_790f8cac1df8",
  "enabled": false,
  "variants": [
    {
      "defaultVariant": false,
      "name": "name_c362910cf198",
      "value": {},
      "percentage": 0,
      "target": "target_c03a7322c14c"
    }
  ],
  "timeWindowStart": "2025-03-14 16:05:28",
  "timeWindowEnd": "2025-03-14 16:05:28"
}
```

### Delete a feature flag

```http
DELETE /flags/{id}
```

## Contributions

Contributions are welcome! If you'd like to contribute, please open a **pull request** with a clear description of your changes.

## License

This project is licensed under the **MIT** License.