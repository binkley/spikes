# Spring Boot Domain Persistence Modeling

* [Concepts](#concepts)
  * [Scoped mutation](#scoped-mutation)
* [Spring-recommended documentation](#spring-recommended-documentation)
  * [Reference documentation](#reference-documentation)
  * [Guides](#guides)

## Concepts

The key concept is _Scoped Mutation_.

## Scoped mutation

Goals:

1. Use _immutable domain objects_ as the default.
2. Provide a simple way to mutate domain objects.
3. Make mutation obvious.
4. Make saving and deleting obvious.

To achieve these goals, each domain object implements
[`ScopedMutation`](src/main/kotlin/x/domainpersistencemodeling/ScopedMutation.kt),
which provides three methods:

* `update(block)`, running the mutations of `block` against a mutable version
  of the domain object, and returning an updated and immutable domain object
* `save()`, saving the domain object in persistence, and returning an updated
  and immutable domain object including any changes made by persistence (eg,
  audit columns)
* `delete()`, deleting the domain object in persistence.  Afterwards, the
  domain object is _unusable_

In each method, the return is the _same reference_ as the original object.
(This supports method chaining, and delegating mutations to another method or
object when it makes sense.)
  
These are the _only ways_ to mutate a domain object.  As a consequence, you
can directly inspect for any domain object mutation by searching for uses of
these three methods, and if there are no uses, the domain object is guaranteed
to have never changed.

## Spring-recommended documentation

### Reference documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.0.RC1/maven-plugin/)
* [Rest Repositories](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#howto-use-exposing-spring-data-repositories-rest-endpoint)
* [Spring Data JDBC](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/)
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#howto-execute-flyway-database-migrations-on-startup)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#production-ready)
* [Atrium](https://docs.atriumlib.org)
* [Test Containers](https://www.testcontainers.org)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
  &mdash; Also applies to Data JDBC
* [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/master/jdbc/basics)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
