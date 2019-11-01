# Micronaut Domain Persistence Modeling

**THIS PROJECT IS PRESENTLY BROKEN** pending support from Micronaut Data for
SQL arrays.

* [Concepts](#concepts)
  * [Scoped mutation](#scoped-mutation)

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
