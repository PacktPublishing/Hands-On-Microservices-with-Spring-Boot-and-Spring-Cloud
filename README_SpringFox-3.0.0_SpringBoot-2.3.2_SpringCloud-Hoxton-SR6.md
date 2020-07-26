<!-- MarkdownTOC -->

- [1. Added support for SpringFox 3.0.0](#1-added-support-for-springfox-300)
- [2. Changes in the source code](#2-changes-in-the-source-code)
  - [2.1. Upgrade to Spring Boot 2.3](#21-upgrade-to-spring-boot-23)
    - [2.1.1. Updates to build files](#211-updates-to-build-files)
    - [2.1.2. Auto creation of index in MongoDB disabled](#212-auto-creation-of-index-in-mongodb-disabled)
    - [2.1.3. HTTP error messages not included by default](#213-http-error-messages-not-included-by-default)
    - [2.1.4. Upgrade to Spring dependency management plugin 1.0.9](#214-upgrade-to-spring-dependency-management-plugin-109)
    - [2.1.5. Changes in test classes](#215-changes-in-test-classes)
    - [2.1.6. Upgrade to Gradle 5.6.4](#216-upgrade-to-gradle-564)
    - [2.1.7. Upgrade to MapStruct 1.3.1](#217-upgrade-to-mapstruct-131)
    - [2.1.8. Remove unused Maven repositories](#218-remove-unused-maven-repositories)
  - [2.2. Upgrade to SpringFox 3.0.0](#22-upgrade-to-springfox-300)
    - [2.2.1. Updates to the build file in the `product-composite-service` project](#221-updates-to-the-build-file-in-the-product-composite-service-project)
    - [2.2.2. Updates to the build file in the `api` project](#222-updates-to-the-build-file-in-the-api-project)
    - [2.2.3. Removed the annotation `@EnableSwagger2WebFlux`](#223-removed-the-annotation-enableswagger2webflux)
  - [2.3. Upgrade to Spring Cloud Hoxton SR6](#23-upgrade-to-spring-cloud-hoxton-sr6)
    - [2.3.1. Updates to build files](#231-updates-to-build-files)
    - [2.3.2. Upgrade dependencies for the authorization server](#232-upgrade-dependencies-for-the-authorization-server)
    - [2.3.3. Upgrade to Resilience4J 1.3.1](#233-upgrade-to-resilience4j-131)
      - [2.3.3.1. Updates to the build file in the `product-composite-service` project](#2331-updates-to-the-build-file-in-the-product-composite-service-project)
      - [2.3.3.2. Updated exception classes](#2332-updated-exception-classes)
      - [2.3.3.3. Enable circuit breaker health information](#2333-enable-circuit-breaker-health-information)
      - [2.3.3.4. Update property name for wait time in the open state](#2334-update-property-name-for-wait-time-in-the-open-state)
      - [2.3.3.5. Changes in the test script `test-em-all.bash`](#2335-changes-in-the-test-script-test-em-allbash)

<!-- /MarkdownTOC -->

# 1. Added support for SpringFox 3.0.0

While this book was written, only snapshot versions were available for SpringFox 3.0.0. Using a snapshot dependency has caused build problems with the source code in the book during the completion of the SpringFox 3.0.0 release. See issue [#2](https://github.com/PacktPublishing/Hands-On-Microservices-with-Spring-Boot-and-Spring-Cloud/issues/2) and [#8](https://github.com/PacktPublishing/Hands-On-Microservices-with-Spring-Boot-and-Spring-Cloud/issues/8) for details. On July 14, 2020, SpringFox 3.0.0 finally was released. The source code for the book is now updated to use SpringFox 3.0.0.

Ths source code in the book is based on Spring Boot 2.1. Since the 3.0.0 release of SpringFox requires at least Spring Boot 2.2, the codebase has been updated to use Spring Boot 2.3.2 and Spring Cloud Hoxton SR6.

The Swagger UI is now available at http://localhost:8080/swagger-ui/index.html.

Added to this, we can get both Swagger and OpenAPI descriptions of the API from the following URL's:

* Swagger: <http://localhost:8080/v2/api-docs>
* OpenAPI: <http://localhost:8080/v3/api-docs>

# 2. Changes in the source code

The source code changes are kept to a minimum to keep the source code as close as possible to the code examples in the book. No new features in Spring Boot 2.3.2 and Spring Cloud Hoxton SR6 are used for this reason.

Modifications made to the source code are described in the following subsections:

* Upgrade to Spring Boot 2.3.2
* Upgrade to SpringFox 3.0.0
* Upgrade to Spring Cloud Hoxton SR6

The changes have bees applied to all chapters where applicable.

## 2.1. Upgrade to Spring Boot 2.3

### 2.1.1. Updates to build files

For details, see the file `build.gradle` in each project.

Replaced the Spring Boot version v2.1.x with v2.3.2, e.g.:

```
    springBootVersion = '2.1.4.RELEASE'
```

With:

```
    springBootVersion = '2.3.2.RELEASE'
```

### 2.1.2. Auto creation of index in MongoDB disabled

Auto index creation is disabled by default in Spring Data MongoDB 3.0 (used by Spring Boot 2.3.2). For details, see <https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#new-features.3.0>. 

To ensure that the expected indexes are created in the MongoDB databases, a method, `initIndicesAfterStartup`, has been added to the application classes of the product and recommendation services. See:
* `microservices/product-service/src/main/java/se/magnus/microservices/core/product/ProductServiceApplication.java`
* `microservices/recommendation-service/src/main/java/se/magnus/microservices/core/recommendation/RecommendationServiceApplication.java`

In Chapter 6, the implementation of the method `initIndicesAfterStartup` is synchronous, i.e. blocking by nature. Starting with Chapter 7, the implementation is reactive, i.e. asynchronous and non-blocking. Since we want to ensure that the indexes are created during the startup phase, a call to the `block` - method is performed after calling the non-blocking method `ensureIndex`.

### 2.1.3. HTTP error messages not included by default

Starting with Spring Boot 2.3.0, the error message and any binding errors are no longer included in the default HTTP error message by default.
For details see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#changes-to-the-default-error-pages-content

The change has been made to avoid leaking information about the server to clients. 

Since the error messages used by this book does not include any sensitive information, only relevant error information, the microservices has been configured to include the error messages.

     
In the `application.yml` file of the four microservices the following configuration has been added:
   
```
server.error.include-message: always
```

From Chapter 12, where the config server is introduced, the configuration has been added to corresponding configuration files in the `config-repo` folder.

### 2.1.4. Upgrade to Spring dependency management plugin 1.0.9

In the `api` and `util` projects, the Spring dependency management plugin has been upgraded to v1.0.9.

Replaced:

```
  id "io.spring.dependency-management" version "1.0.5.RELEASE"
```

With:

```
  id 'io.spring.dependency-management' version '1.0.9.RELEASE'
```  

### 2.1.5. Changes in test classes

1. **Replaced media type `APPLICATION_JSON_UTF`**

    Replaced the deprecated and removed media type `APPLICATION_JSON_UTF8` with `APPLICATION_JSON`.

    Replaced:

    ```
    import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
    ```

    With:

    ```
    import static org.springframework.http.MediaType.APPLICATION_JSON;
    ```

1. **Changes in `ProductCompositeServiceApplicationTests.java`**

    Since the `WebTestClient` got new `body` methods in Spring Boot 2.3, calls to its body methods has been updated to specify which of the methods to use.

    See line 129 in: `microservices/product-composite-service/src/test/java/se/magnus/microservices/composite/product/ProductCompositeServiceApplicationTests.java`


    Replaced:

    ```
          .body(just(compositeProduct), null)
    ```

    With:

    ```
          .body(just(compositeProduct), ProductAggregate.class)
    ```

### 2.1.6. Upgrade to Gradle 5.6.4

Spring Boot 2.3.2 requires Gradle 5.6.4 or higher.

See `gradle/wrapper/gradle-wrapper.properties`.

### 2.1.7. Upgrade to MapStruct 1.3.1

Spring Boot 2.3.2 depends on  MapStruct 1.3.1.

Applicable `build.gradle` files have been updated.

Replaced:

```
    mapstructVersion = "1.3.0.Final"
```

With:

```
    mapstructVersion = "1.3.1.Final"
```

### 2.1.8. Remove unused Maven repositories

Dependencies to snapshot and milestone Maven repositories have been removed from all  `build.gradle` files.


## 2.2. Upgrade to SpringFox 3.0.0

Changes apply from Chapter 5 onwards.

The source code is updated according to [Migrating from earlier snapshot](https://github.com/springfox/springfox#migrating-from-earlier-snapshot).

### 2.2.1. Updates to the build file in the `product-composite-service` project

For details, see the file `microservices/product-composite-service/build.gradle`.

Removed:

```
  springfoxVersion = "3.0.0-20190808.104142"
```

Replaced:

```
  implementation("io.springfox:springfox-swagger2:${springfoxVersion}-39")
  implementation("io.springfox:springfox-swagger-ui:${springfoxVersion}-39")
  implementation("io.springfox:springfox-swagger-common:${springfoxVersion}-39")
  implementation("io.springfox:springfox-spring-webflux:${springfoxVersion}-31")
  implementation("io.springfox:springfox-spi:${springfoxVersion}-39")
  implementation("io.springfox:springfox-core:${springfoxVersion}-39")
  implementation("io.springfox:springfox-schema:${springfoxVersion}-39")
  implementation("io.springfox:springfox-spring-web:${springfoxVersion}-38")
```

With:

```
  implementation('io.springfox:springfox-boot-starter:3.0.0')
```

### 2.2.2. Updates to the build file in the `api` project

For details, see the file `api/build.gradle`.

Replaced:

```
  implementation('io.springfox:springfox-swagger2:3.0.0-SNAPSHOT')
```

With:

```
  implementation('io.springfox:springfox-swagger2:3.0.0')
```

### 2.2.3. Removed the annotation `@EnableSwagger2WebFlux`

The annotation `@EnableSwagger2WebFlux` is no longer required and has been removed from the application class. 

For detail see `microservices/product-composite-service/src/main/java/se/magnus/microservices/composite/product/ProductCompositeServiceApplication.java`

## 2.3. Upgrade to Spring Cloud Hoxton SR6

Changes apply from Chapter 9 onwards.
### 2.3.1. Updates to build files

For details, see the file `build.gradle` in each project.

Replaced the Spring Cloud Greenwich versions with Hoxton.SR6, e.g.:

```
  springCloudVersion = "Greenwich.SR2"
```

With:

```
  springCloudVersion = "Hoxton.SR6"
```

### 2.3.2. Upgrade dependencies for the authorization server

Dependencies on various Spring Cloud components have been upgraded to the corresponding versions in the Hoxton SR6 release. Unused dependencies to JAXB have been removed.

For details, see `spring-cloud/authorization-server/build.gradle`.

Replaced:

```
  implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:2.1.1.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-config:2.1.1.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-sleuth:2.1.1.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-zipkin:2.1.1.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit:2.1.1.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka:2.1.1.RELEASE'
```

With:

```
  implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:2.2.3.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-config:2.2.3.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-sleuth:2.2.3.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-zipkin:2.2.3.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit:3.0.6.RELEASE'
  implementation 'org.springframework.cloud:spring-cloud-starter-stream-kafka:3.0.6.RELEASE'
```

Removed:

```
  implementation 'javax.xml.bind:jaxb-api'
  implementation 'com.sun.xml.bind:jaxb-core'
  implementation 'com.sun.xml.bind:jaxb-impl'
```

### 2.3.3. Upgrade to Resilience4J 1.3.1

Changes apply from Chapter 14 onwards.

Spring Cloud Hoxton SR6 depends on Resilience4J v1.3.1, which has some backward compatibility issues with the version used in the book, v0.14.1:

1. Exceptions replaced
2. Configuration changes
3. New API's for monitoring and health checks

#### 2.3.3.1. Updates to the build file in the `product-composite-service` project

For details, see the file `microservices/product-composite-service/build.gradle`.

Replaced:

```
  resilience4jVersion = "0.14.1"
```

With:

```
  resilience4jVersion = "1.3.1"
```

#### 2.3.3.2. Updated exception classes

The exception class `CallNotPermittedException` is replaced by the exception class `CircuitBreakerOpenException`, and we no longer need to catch and unwrap the exception class `RetryExceptionWrapper`.

For details, see `microservices/product-composite-service/src/main/java/se/magnus/microservices/composite/product/services/ProductCompositeServiceImpl.java`.

Replaced:

```
                    .onErrorMap(RetryExceptionWrapper.class, retryException -> retryException.getCause())
                    .onErrorReturn(CircuitBreakerOpenException.class, getProductFallbackValue(productId)),

```

With:

```
                    .onErrorReturn(CallNotPermittedException.class, getProductFallbackValue(productId)),
```

#### 2.3.3.3. Enable circuit breaker health information

The source code in the book is based on that the Resilience4J circuit breaker reports its state in the Actuator health-endpoint. This is disabled by default in Resilience4J v1.3.1. To enable it, the following property is added in `config-repo/product-composite.yml`:

Added:

```
management.health.circuitbreakers.enabled: true
```

#### 2.3.3.4. Update property name for wait time in the open state

The name of configuration property for specifying how long the circuit breaker is retained in the open state has changed.

It is reflected in `config-repo/product-composite.yml` by replacing:

```
waitInterval:
```

With:

```
waitDurationInOpenState:
```

<!--
**TO DO**: Review the rest of the CB-parameters! 

```
resilience4j.circuitbreaker:
  backends:
    product:
      registerHealthIndicator: true
      ringBufferSizeInClosedState: 5
      failureRateThreshold: 50
      waitInterval: 10000
      ringBufferSizeInHalfOpenState: 3
      automaticTransitionFromOpenToHalfOpenEnabled: true
      ignoreExceptions:
        - se.magnus.util.exceptions.InvalidInputException
        - se.magnus.util.exceptions.NotFoundException
```

**Chapter 20:ARE YOU SURE??? ISN'T IT FOR THE CADEC2020 BRANCH!?**

Remove:

```
management.health.circuitbreakers.enabled: true
```
-->

#### 2.3.3.5. Changes in the test script `test-em-all.bash`

Due to changes in Resilience4J information reported in the health endpoint, the following change has been performed.

Replaced (in 3 places):

```
jq -r .details.productCircuitBreaker.details.state
```

With:

```
jq -r .components.circuitBreakers.details.product.details.state
```