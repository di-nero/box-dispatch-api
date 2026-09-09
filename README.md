# Box Dispatch API

## Overview

This project is a REST API for managing items through a box-based delivery system.

Items are added to dispatch boxes, while the boxes have different states representing their position in the delivery process. The API allows users to create boxes, load items, check items in a box, check available boxes, and check the battery level of a particular box.

## Features

* Create dispatch boxes
* Automatically generate a unique transaction reference (`txref`)
* Load items into boxes
* Retrieve items loaded in a box
* Check the battery level of a box
* Check boxes available for loading
* Validate item names, weights, and codes
* Enforce box weight limits
* Enforce the minimum 25% battery requirement for loading
* Validate box states before loading
* Global exception handling
* Unit testing with JUnit and Mockito
* Integration/E2E testing with Testcontainers and PostgreSQL
* Swagger/OpenAPI documentation

## Technologies

* Java 17
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* PostgreSQL
* Maven
* JUnit
* Mockito
* Testcontainers
* Swagger / OpenAPI
* Lombok

## Live API

The API is deployed and available at:

https://box-dispatch-api.onrender.com

You can use the live API directly with the cURL examples below.

### 1. Create a Box

**POST** `/boxes`

Creates a new dispatch box.

#### Request

```json
{
  "weightLimit": 500
}
```

The `weightLimit` must not exceed 500g.

#### cURL

```bash
curl -X POST https://box-dispatch-api.onrender.com/boxes \
  -H "Content-Type: application/json" \
  -d '{
    "weightLimit": 500
  }'
```

#### Response

```json
{
  "id": "generated-uuid",
  "txref": "BOX-A82F91C3",
  "weightLimit": 500,
  "batteryCapacity": 100,
  "state": "IDLE"
}
```

A new box is automatically assigned a unique `txref`, starts with 100% battery, and has an initial state of `IDLE`.


### 2. Load Items Into a Box

**POST** `/boxes/{txref}/items`

Loads one or more items into a specified box.

#### Request

```json
{
  "items": [
    {
      "name": "Phone-1",
      "weight": 100,
      "code": "PHONE_001"
    }
  ]
}
```

The `txref` identifies the box to load.

If the request is successful, the box state changes to `LOADED`.
#### cURL

```bash
curl -X POST https://box-dispatch-api.onrender.com/boxes/BOX-A82F91C3/items \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "name": "Phone-1",
        "weight": 100,
        "code": "PHONE_001"
      }
    ]
  }'
```
#### Response

```json
{
  "id": "generated-uuid",
  "txref": "BOX-A82F91C3",
  "weightLimit": 500,
  "batteryCapacity": 100,
  "state": "LOADED"
}
```
---

---

### 3. Get Items in a Box

**GET** `/boxes/{txref}/items`

Returns all items currently loaded in the specified box.
#### cURL

```bash
curl https://box-dispatch-api.onrender.com/boxes/BOX-A82F91C3/items
```

#### Response

```json
[
  {
    "id": "generated-uuid",
    "name": "Phone-1",
    "weight": 100,
    "code": "PHONE_001"
  }
]
```

---
---

### 4. Get Available Boxes

**GET** `/boxes/available?page=0&size=20`

Returns a paginated list of boxes that are currently available for loading. `page` and `size` are optional (defaults: `page=0`, `size=20`).

A box is considered available when:

* Its battery level is at least 25%.
* Its state is `IDLE`, `LOADING`, or `LOADED`.
* Its current weight is below its weight limit.
#### cURL

```bash
curl https://box-dispatch-api.onrender.com/boxes/available
```

#### Response

```json
{
  "content": [
    {
      "id": "generated-uuid",
      "txref": "BOX-A82F91C3",
      "weightLimit": 500,
      "batteryCapacity": 100,
      "state": "IDLE"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Note: `totalElements`/`totalPages` reflect boxes matching the battery/state criteria; boxes additionally excluded for being weight-full are filtered out of `content` but may still be counted here.

---

---

### 5. Get Box Battery Level

**GET** `/boxes/{txref}/battery`

Returns the current battery level of the specified box.

Example response:

```text
100
```
#### cURL

```bash
curl https://box-dispatch-api.onrender.com/boxes/BOX-A82F91C3/battery
```

#### Response

```text
100
```


## Business Rules

### Box Weight Limit

A box cannot contain items whose combined weight exceeds the box's configured weight limit.

The maximum weight limit that can be assigned to a box is 500g.

If loading the requested items would exceed the limit, the API returns a `400 Bad Request`.

Example:

```json
{
  "message": "Box weight limit exceeded",
  "status": 400
}
```

### Battery Requirement

A box must have a battery level of at least 25% before it can be loaded.

If the battery level is below 25%, the API returns a `400 Bad Request`.

### Box State

Items can only be loaded when the box is in one of the following states:

* `IDLE`
* `LOADING`
* `LOADED`

A box cannot be loaded when it is in:

* `DELIVERING`
* `DELIVERED`
* `RETURNING`

### Item Validation

Item names can contain only:

* Letters
* Numbers
* Hyphens (`-`)
* Underscores (`_`)

Item weight must be greater than 0g.

Item codes can contain only:

* Uppercase letters
* Numbers
* Underscores (`_`)

Invalid item data results in a `400 Bad Request`.

### Box Not Found

If a requested `txref` does not exist, the API returns a `404 Not Found`.

Example:

```json
{
  "message": "Box not found!",
  "status": 404
}
```

## Getting Started

### Prerequisites

Before running the application, make sure you have the following installed:

* Java 17
* Maven
* PostgreSQL
* IntelliJ IDEA
* Postman (for API testing)

### Installation

1. Clone the repository from GitHub.
2. Open the project in IntelliJ IDEA.
3. Create a PostgreSQL database for the application.
4. Configure the database connection details in `application.yml` or `application.properties`.
5. Run the application.

### Database Configuration

The application uses PostgreSQL as its database.

`src/main/resources/application.yaml` is committed to the repo and reads every setting from an environment variable, falling back to a sensible local default if the variable isn't set - so the app runs out of the box against a local Postgres instance with no configuration required.

Default configuration:

* **Database:** `box_dispatch`
* **Username:** `postgres`
* **PostgreSQL Port:** `5432`
* **Application Port:** `8080`

To override any of these, copy `.env.example` to `.env` and adjust values, or export the variables directly before running the app:

```
DB_URL=jdbc:postgresql://<host>:5432/<dbname>
DB_USERNAME=<username>
DB_PASSWORD=<password>
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
SERVER_PORT=8080
```

### Running the Application

The application can be run through IntelliJ IDEA or Maven.

#### Using IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Locate the main Spring Boot application class.
3. Click the green **Run ▶** button.
4. The application will start on port `8080`.

#### Using Maven

From the project root directory, run:

```bash
mvn spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

## Testing

The project includes unit tests and integration/E2E tests.

### Unit Tests

Unit tests are written using **JUnit and Mockito** to test the service-layer business logic independently of the database.

### Integration/E2E Tests

Integration/E2E tests use **Spring Boot, MockMvc, Testcontainers, and PostgreSQL** to test the API endpoints with a real PostgreSQL database running inside a Docker container.

The tests cover scenarios such as:

* Creating a box
* Loading items into a box
* Retrieving items from a box
* Checking battery levels
* Retrieving available boxes
* Handling invalid item data
* Enforcing box weight limits
* Handling non-existent boxes

### Running Tests

Run all tests using Maven:

```bash
mvn test
```

You can also run the tests directly from IntelliJ IDEA using the **Run ▶** button.

> Docker must be running when executing the integration/E2E tests because Testcontainers uses Docker to create the PostgreSQL test container.

## Swagger Documentation

The API is documented using Swagger/OpenAPI.

After starting the application locally, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The deployed Swagger UI is also available at:

```text
https://box-dispatch-api.onrender.com/swagger-ui/index.html
```

Swagger UI provides an interactive interface for viewing the available endpoints, request/response formats, and testing the API directly.

## Project Structure

```text
src/
  main/
    java/
      com.example.box_dispatch_api/
        config/
        controller/
        dto/
        entity/
        exception/
        repository/
        service/
        util/
    resources/
      application.yaml
  test/
    java/
      com.example.box_dispatch_api/
        integration/
        service/
```

### Package Responsibilities

* **controller** — Handles HTTP requests and responses.
* **service** — Contains the business logic and communicates with repositories.
* **repository** — Handles database access through Spring Data JPA.
* **entity** — Contains the database entities such as `Box` and `Item`.
* **dto** — Contains request and response objects used by the API.
* **exception** — Contains custom exceptions and global exception handling.
* **config** — Contains application configuration such as OpenAPI/Swagger configuration.
* **util** — Contains utility classes such as the transaction reference generator.
* **test** — Contains unit and integration/E2E tests.

The application follows the request flow:

`HTTP Request → Controller → Service → Repository → Database`

The response then follows the reverse path back to the client.

## Assumptions and Scope

The following assumptions and implementation decisions were made where the assessment did not explicitly define the behavior:

* `txref` is automatically generated and unique.
* New boxes start with 100% battery.
* New boxes start in the `IDLE` state.
* Battery capacity ranges from 0–100%.
* Item weight must be greater than 0g.
* The maximum box weight limit is 500g.
* A box must have at least 25% battery to be loaded.
* Only `IDLE`, `LOADING`, and `LOADED` boxes can accept items.
* Loading items changes the box state directly to `LOADED`.
* Delivery and return operations are outside the scope of this assessment.
* Battery consumption is outside the scope because no battery consumption formula or delivery operation was specified.
* Available-box responses do not include the items currently loaded in each box.
* Authentication, authorization, and caching (e.g. Redis) were intentionally not implemented because they were not required by the assessment and add no value at this scope - there is nothing here that benefits from caching, and no user/session concept to authenticate.
* Loading a box is wrapped in a transaction and takes a pessimistic row lock on the box for the duration of the request, so concurrent load requests against the same box cannot both pass the weight-limit check and jointly overload it.

## Future Improvements

If this project were extended beyond the assessment, the following features could be added:

* Authentication and authorization
* Battery consumption tracking
* Delivery and return operations (which would exercise the `DELIVERING`/`DELIVERED`/`RETURNING` states - currently unreachable via the API since no endpoint transitions a box into them)
* More detailed API error responses
* Application monitoring and logging
