# Spring Boot Domain Persistence Modeling

* [Concepts](#concepts)
  * [Separation of concerns](#separation-of-concerns)
  * [Scoped mutation](#scoped-mutation)
  * [Reversal of roles](#reversal-of-roles)
  * [Distinct types](#distinct-types)
* [Spring-recommended documentation](#spring-recommended-documentation)
  * [Reference documentation](#reference-documentation)

## Concepts

The key concepts are:

- [_Scoped mutation_](#scoped-mutation)
- [_Reversal of roles_](#reversal-of-roles)

## Separation of concerns

A classic programming thought, _separation of concerns_ is important for clean
code, often expressed in the OOP community as the
[Single Responsibility Principle](https://blog.cleancoder.com/uncle-bob/2014/05/08/SingleReponsibilityPrinciple.html).

Here that entails dividing properties of an entity at different levels:

- Simple values common to all levels
- Representations specific to persistence
- Representations specific to domain modeling
- Computed values in the domain implementation

## Scoped mutation

Goals:

1. Use _immutable domain objects_ as the default.
2. Provide a simple way to mutate domain objects.
3. Make mutation obvious.
4. Make saving and deleting obvious.

To achieve these goals, each domain object implements
[`ScopedMutable`](src/main/kotlin/x/domainpersistencemodeling/ScopedMutable.kt),
which provides three methods:

* `ScopedMutable.update(block)` runs the mutations of `block` against a
  mutable version of the domain object, and returns an updated and immutable
  domain object

In the method, the return is the result of the block.

In addition, there are several specialized scoped-mutation methods:

* `PersistableDomain.save()` saves the domain object in persistence, and
  returns an updated and immutable domain object including any changes made
  by persistence (eg, versioning).
* `PersistableDomain.delete()` deletes the domain object in persistence.
  After this call, the domain object is _unusable_.

A domain object may have specialized scoped-mutation methods particular to
itself:

* `Parent.assign(Child)` mutates both parent and child, but does not persist
* `Parent.unassign(Child)` mutates both parent and child, but does not persist

## Reversal of roles

Commonly domain objects refer to objects from other domains, however, at the
persistence level, the relationship represented differently.  In this code,
the key example is:

* (Domain) A parent owns zero or more children, and children do not know about
  parents
* (Persistence) A child record knows to which parent record it belongs (if
  any), and parents do not know about children

Why this reversal?  At the domain level, activities center around the parent,
and one wishes to manage parent-child relationships through them.  However,
these domain objects are _aggregate roots_ at the persistence level.

Using this reversal of roles, saving a parent is an *O(N)* operation,
proportional to the count of children, and with some simple optimizations,
can be *O(1)* if changing only satellite data on the parent, or changing only
a single parent-child relationship (adding or removing). 

Consider the **contrary** persistence representation: parent records track
their child records:

```kotlin
data class ParentRecord( // Contrary case -- **do not do this**
    var naturalKey: String,
    var someValue: Int,
    var children: Set<ChildRecord>)
data class ChildRecord(
    var naturalKey: String,
    var parentNaturalKey: String?,
    var others: Set<AnotherRecordType>)
```

When saving `ParentRecord`, a framework like Spring Data performs these
operations in some order:

1. Deletes all existing `ChildRecord` rows.  This is a recursive operation, so
   any record references held by children are themselves recursively deleted.
   This clears the parent record of any previous child record references,
   including: children added, children removed, children modified. 
2. Saves the `ParentRecord` row to a store (such as an SQL database).
3. Inserts all `ChildRecord` rows for the current `ParentRecord`.  Again, this
   is a recursive operation (as above).  This updates the persistence store to
   match current parent-child relationships.

In this contrary case, we are ignoring that `ChildRecord` is itself an
_aggregate root_, creating large amounts of persistence churn.  In a scenario
when adding children to a parent one at a time (say from an external source
that does not add all children in a single operation), the overall
operation of saving a parent has become an *O(N<sup>2</sup>)* operation.
A parent with roughly 3,000 children results in a multiple of 10MM persistence
operations.

In this **contrary** persistence representation, saving a parent bears this
cost even when updating satellite data on the parent record.

## Distinct types

To aid in avoiding programming mistakes, and catch such errors in complilation
rather than at runtime, we divide child domain objects into two types:
"unassigned" (`UnassignedChild`) and "assigned" (`AssignedChild`).  As the JVM
and JVM-based languages such as Kotlin and Java do not support casting based
on memory layout (unlike "C" and C++), the code needs to construct distinct
objects when converting between "unassigned" and "assigned".

There is an upside: This prevents misuse of the wrong type through their
common base type as conversion creates a new object.

One downside: This violates the rule that only `update` mutates objects.
Rather, for children, `update`, `assignTo`, and `unassignFromAny` all mutate.

## Spring-recommended documentation

### Reference documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
  &mdash; After much trial with both Gradle and Maven, this spike uses Maven
  for:
  1. More repeatable builds
  2. Simplicity of build configuration
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.0.RC1/maven-plugin/)
* [Spring Data JDBC](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/)
  &mdash; Simple persistence based on _aggregate roots_
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#howto-execute-flyway-database-migrations-on-startup)
  &mdash; Simple installation of SQL schemas (unclear if it can be even
  simpler)
* [Atrium](https://docs.atriumlib.org) &mdash; `expect`-style assertions for
  Kotlin
* [Test Containers](https://www.testcontainers.org) &mdash; runs tests using
  Postgres in a Docker container
