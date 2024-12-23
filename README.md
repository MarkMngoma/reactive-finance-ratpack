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
  /v1/QueryCurrencyResource/Exchanges/{currencyCode}:
    get:
      tags:
        - V1
      summary: /v1/QueryCurrencyResource/exchanges
      description: /v1/QueryCurrencyResource/exchanges
      operationId: v1QuerycurrencyresourceExchanges
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
  /v1/WriteBatchCurrencyResource:
    post:
      tags:
        - V1
      summary: /v1/WriteBatchCurrencyResource
      description: /v1/WriteBatchCurrencyResource
      operationId: v1Writebatchcurrencyresource
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                batchCurrencies:
                  type: array
                  items:
                    type: object
                    properties:
                      currencyCode:
                        type: string
                        example: XOF
                      currencyFlag:
                        type: string
                        example: 🇸🇳
                      currencyId:
                        type: number
                        example: 952
                      currencyName:
                        type: string
                        example: CFA Franc BCEAO (West African CFA franc)
                      currencySymbol:
                        type: string
                        example: CFA
                  example:
                    - currencyCode: XOF
                      currencyFlag: 🇸🇳
                      currencyId: 952
                      currencyName: CFA Franc BCEAO (West African CFA franc)
                      currencySymbol: CFA
                    - currencyCode: XAF
                      currencyFlag: 🇨🇲
                      currencyId: 950
                      currencyName: CFA Franc BEAC (Central African CFA franc)
                      currencySymbol: FCFA
                    - currencyCode: ZAR
                      currencyFlag: 🇿🇦
                      currencyId: 710
                      currencyName: South African Rand
                      currencySymbol: R
                    - currencyCode: NGN
                      currencyFlag: 🇳🇬
                      currencyId: 566
                      currencyName: Nigerian Naira
                      currencySymbol: ₦
                    - currencyCode: KES
                      currencyFlag: 🇰🇪
                      currencyId: 404
                      currencyName: Kenyan Shilling
                      currencySymbol: KSh
                    - currencyCode: UGX
                      currencyFlag: 🇺🇬
                      currencyId: 800
                      currencyName: Ugandan Shilling
                      currencySymbol: USh
                    - currencyCode: GHS
                      currencyFlag: 🇬🇭
                      currencyId: 936
                      currencyName: Ghanaian Cedi
                      currencySymbol: ₵
                    - currencyCode: TZS
                      currencyFlag: 🇹🇿
                      currencyId: 834
                      currencyName: Tanzanian Shilling
                      currencySymbol: TSh
                    - currencyCode: ZMW
                      currencyFlag: 🇿🇲
                      currencyId: 967
                      currencyName: Zambian Kwacha
                      currencySymbol: ZK
                    - currencyCode: BWP
                      currencyFlag: 🇧🇼
                      currencyId: 58
                      currencyName: Botswana Pula
                      currencySymbol: P
                    - currencyCode: SCR
                      currencyFlag: 🇸🇨
                      currencyId: 690
                      currencyName: Seychellois Rupee
                      currencySymbol: ₨
                    - currencyCode: NAD
                      currencyFlag: 🇳🇦
                      currencyId: 516
                      currencyName: Namibian Dollar
                      currencySymbol: $
                    - currencyCode: MUR
                      currencyFlag: 🇲🇺
                      currencyId: 480
                      currencyName: Mauritian Rupee
                      currencySymbol: ₨
                    - currencyCode: MWK
                      currencyFlag: 🇲🇼
                      currencyId: 454
                      currencyName: Malawian Kwacha
                      currencySymbol: MK
                    - currencyCode: GMD
                      currencyFlag: 🇬🇲
                      currencyId: 270
                      currencyName: Gambian Dalasi
                      currencySymbol: D
                    - currencyCode: SZL
                      currencyFlag: 🇸🇿
                      currencyId: 748
                      currencyName: Swazi Lilangeni (Swaziland)
                      currencySymbol: E
                    - currencyCode: RWF
                      currencyFlag: 🇷🇼
                      currencyId: 646
                      currencyName: Rwandan Franc
                      currencySymbol: RF
            examples:
              /v1/WriteBatchCurrencyResource:
                value:
                  batchCurrencies:
                    - currencyCode: ZAR
                      currencyFlag: 🇿🇦
                      currencyId: 710
                      currencyName: South African Rand
                      currencySymbol: R
                    - currencyCode: USD
                      currencyFlag: 🇺🇸
                      currencyId: 840
                      currencyName: United States Dollar
                      currencySymbol: $
                    - currencyCode: GBP
                      currencyFlag: 🇬🇧
                      currencyId: 826
                      currencyName: British Pound Sterling
                      currencySymbol: £
                    - currencyCode: AED
                      currencyFlag: 🇦🇪
                      currencyId: 784
                      currencyName: United Arab Emirates Dirham
                      currencySymbol: د.إ
                    - currencyCode: SAR
                      currencyFlag: 🇸🇦
                      currencyId: 682
                      currencyName: Saudi Riyal
                      currencySymbol: ر.س
                    - currencyCode: KWD
                      currencyFlag: 🇰🇼
                      currencyId: 414
                      currencyName: Kuwaiti Dinar
                      currencySymbol: د.ك
                    - currencyCode: QAR
                      currencyFlag: 🇶🇦
                      currencyId: 634
                      currencyName: Qatari Riyal
                      currencySymbol: ر.ق
                    - currencyCode: OMR
                      currencyFlag: 🇴🇲
                      currencyId: 512
                      currencyName: Omani Rial
                      currencySymbol: ر.ع
                    - currencyCode: BHD
                      currencyFlag: 🇧🇭
                      currencyId: 48
                      currencyName: Bahraini Dinar
                      currencySymbol: ب.د
                    - currencyCode: ILS
                      currencyFlag: 🇮🇱
                      currencyId: 376
                      currencyName: Israeli New Shekel
                      currencySymbol: ₪
                    - currencyCode: TRY
                      currencyFlag: 🇹🇷
                      currencyId: 949
                      currencyName: Turkish Lira
                      currencySymbol: ₺
      responses:
        '200':
          description: ''
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

1. **Run Tests Using Gradle CLI:**

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

### 3. **GET /v1/QueryCurrencyResource/Exchanges/{currencyCode}**

- **Description**: Retrieve the latest exchange rates for all currencies.

### 4. **GET /v1/QueryCurrencyResource/{currencyCode}**

- **Description**: Retrieve a specific currency resource by currency code.
- **Path Parameter**: `currencyCode` (e.g., `ZAR` for South African Rand).

---

Happy coding! 🎉🚀
