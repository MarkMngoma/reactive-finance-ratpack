# 🏦 **Reactive Ratpack Finance API** 🚀

Welcome to the **Reactive Ratpack Finance** API! This is a powerful API for managing and querying currency resources with cutting-edge features built using **Ratpack (Java 17) and Google Guice**.

The API allows you to perform CRUD operations on currency resources, including retrieving currency details, exchanging rates, and more! 🌍💸

## Features 🌟

- **WriteCurrencyResource**: Create a new currency resource.
- **QueryCurrencyResource**: Retrieve currency resources by various filters.
- **Exchange Rates**: Get the latest exchange rates for different currencies.
- **Swagger Spec**: Full OpenAPI 3.0 specification to help you integrate smoothly.

## 📜 API Documentation

The API exposes several endpoints that you can use to interact with the currency resources:

### 📝 **Swagger Spec (OpenAPI 3.0)**

You can find the complete Swagger spec for the API in the `spec.yaml` file. Here's a preview:

```yaml
openapi: 3.0.3
info:
  title: reactive-finance
  version: 1.0.0
  contact: {}
servers:
  - url: https://localhost:5051
  - url: https://proxy.rest.localhost.com/WSF
paths:
  /v1/WriteCurrencyResource:
    post:
      tags:
        - V1
      summary: /v1/WriteCurrencyResource
      description: /v1/WriteCurrencyResource
      operationId: v1Writecurrencyresource
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                currencyCode:
                  type: string
                  example: CHF
                currencyFlag:
                  type: string
                  example: 🇨🇭
                currencyId:
                  type: number
                  example: 756
                currencyName:
                  type: string
                  example: Swiss Franc
                currencySymbol:
                  type: string
                  example: CHF
            examples:
              /v1/WriteCurrencyResource:
                value:
                  currencyCode: CHF
                  currencyFlag: 🇨🇭
                  currencyId: 756
                  currencyName: Swiss Franc
                  currencySymbol: CHF
      responses:
        '200':
          description: ''
  /v1/QueryCurrencyResource:
    get:
      tags:
        - V1
      summary: /v1/QueryCurrencyResource
      description: /v1/QueryCurrencyResource
      operationId: v1Querycurrencyresource
      responses:
        '200':
          description: ''
  /v1/QueryCurrencyResource/exchanges:
    get:
      tags:
        - V1
      summary: /v1/QueryCurrencyResource/exchanges
      description: /v1/QueryCurrencyResource/exchanges
      operationId: v1QuerycurrencyresourceExchanges
      responses:
        '200':
          description: ''
  /v1/QueryCurrencyResource/{currencyCode}:
    get:
      tags:
        - V1
      summary: /v1/QueryCurrencyResource/:currencyCode
      description: /v1/QueryCurrencyResource/:currencyCode
      operationId: v1QuerycurrencyresourceCurrencycode
      responses:
        '200':
          description: ''
    parameters:
      - name: currencyCode
        in: path
        required: true
        schema:
          type: string
          example: ZAR
        description: South African Rands
tags:
  - name: V1
```

## 🚀 Getting Started

To get started with **Reactive Ratpack Finance API**, follow the steps below to set up the project locally or in a containerized environment.

### 🐳 Spinning Up the Database Container

The project uses **Docker** for containerization. To run the application in a container, you need to spin up the Docker container using the provided `docker-compose.yml` file.

1. **Spin Up the localhost Containers:**

   Run the following command to start the containers:

   ```bash
   docker compose -f ./src/test/resources/docker-compose.yml up
   ```

   Run the following command to start the containers in detached mode:

   ```bash
   docker compose -f ./src/test/resources/docker-compose.yml up -d
   ```

2. **Shut Down the Containers:**

   Run the following command to stop the containers:

   ```bash
   docker compose -f ./src/test/resources/docker-compose.yml down
   ```

   This will start all the necessary services defined in the `docker-compose.yml` file.

---

### 🖥️ Running the API on Localhost

1. **Clone the Repository:**

   Clone the repository to your local machine:

   ```bash
   git clone git@github.com:MarkMngoma/reactive-finance-ratpack.git
   cd reactive-finance-ratpack
   ```

2. **Compile ShadowJar:**

   This will automatically pull the required modules

   ```bash
   ./gradlew clean test shadowJar
   ```

3. **Export Environment Variable:**

   This will be read as a java environment variable

   ```bash
   export RATPACK_ENVIRONMENT=localhost
   ```

4. **Start the API Server locally:**

   Using the jar artifact:
   ```bash
   java -jar ./build/libs/reactive-finance-ratpack.jar
   ```

   Using gradle run task
    ```bash
    ./gradlew clean run
    ```

   The API will now be available at [http://localhost:5051]([http://localhost:5051).

---

### 🔬 Running Integration Tests

The project includes integration tests to ensure that everything is functioning as expected.

1. **Run Tests Using .NET CLI:**

   To run the integration tests, use the following command:

   ```bash
   ./gradlew clean test
   ```

   This will execute all the integration tests and display the results in the terminal.

2. **Test Environment Setup:**

   Make sure the API is running before executing the tests, especially when working with containers. You can also run the tests in isolation or as part of your CI/CD pipeline.

---

## 🧑‍💻 API Endpoints

### 1. **POST /v1/WriteCurrencyResource**

- **Description**: Create a new currency resource.
- **Request Body**:
  ```json
  {
    "currency_code": "CHF",
    "currency_flag": "🇨🇭",
    "currency_id": 756,
    "currency_name": "Swiss Franc",
    "currency_symbol": "CHF"
  }
  ```

### 2. **GET /v1/QueryCurrencyResource**

- **Description**: Retrieve all currency resources.
- **Response**: List of all currencies.

### 3. **GET /v1/QueryCurrencyResource/exchanges**

- **Description**: Retrieve the latest exchange rates for all currencies.

### 4. **GET /v1/QueryCurrencyResource/{currencyCode}**

- **Description**: Retrieve a specific currency resource by currency code.
- **Path Parameter**: `currencyCode` (e.g., `ZAR` for South African Rand).

---

Happy coding! 🎉🚀
