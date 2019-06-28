# Spring Boot transations

Demonstrate Spring transaction features

* [An asynchronous, post-commit event listener](src/test/java/x/txns/DatabaseTest.java)
* [Serailized read access with `SELECT ... FOR UPDATE`](src/test/java/x/txns/RowLockTest.java)

## Running the demonstration

1. Ensure Docker is running locally.
2. Run `docker-compose up`.
3. Run `./gradlew bootRun`.

Relevant output of first run of `./gradlew bootRun`:

```
2019-05-06 18:19:28.717  INFO 17046 --- [           main] x.txns.WhenReady                         : SAVED: Foo(id=1, key=BAR, value=3)
2019-05-06 18:19:28.738  INFO 17046 --- [           main] x.txns.WhenReady                         : PUBLISHED: FooEvent(super=x.txns.FooEvent[source=Foo(id=1, key=BAR, value=3)])
2019-05-06 18:19:28.738  INFO 17046 --- [           main] x.txns.WhenReady                         : END OF PUBLISHING
2019-05-06 18:19:28.759  INFO 17046 --- [         task-1] x.txns.FooListener                       : RECEIVED POST-COMMIT: Foo(id=1, key=BAR, value=3)
```

Relevant output of second run of `./gradlew bootRun`:

```
2019-05-06 18:19:52.423  INFO 17095 --- [           main] x.txns.WhenReady                         : SAVED: Foo(id=2, key=BAR, value=3)
2019-05-06 18:19:52.443  INFO 17095 --- [           main] x.txns.WhenReady                         : PUBLISHED: FooEvent(super=x.txns.FooEvent[source=Foo(id=1, key=BAR, value=3)])
2019-05-06 18:19:52.444  INFO 17095 --- [           main] x.txns.WhenReady                         : PUBLISHED: FooEvent(super=x.txns.FooEvent[source=Foo(id=2, key=BAR, value=3)])
2019-05-06 18:19:52.444  INFO 17095 --- [           main] x.txns.WhenReady                         : END OF PUBLISHING
2019-05-06 18:19:52.469  INFO 17095 --- [         task-2] x.txns.FooListener                       : RECEIVED POST-COMMIT: Foo(id=2, key=BAR, value=3)
2019-05-06 18:19:52.469  INFO 17095 --- [         task-1] x.txns.FooListener                       : RECEIVED POST-COMMIT: Foo(id=1, key=BAR, value=3)
```
