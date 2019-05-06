# Spring Boot transations

Demonstrate an asynchronous, post-commit event listener.

## Running the demonstration

1. Ensure Docker is running locally.
2. Run `docker-compose up`.
3. Run `./gradlew bootRun`.

Relevant output of first run of `./gradlew bootRun`:

```
2019-05-06 17:56:06.695  INFO 16581 --- [           main] x.txns.WhenReady                         : SAVED: Foo(id=1, key=BAR, value=3)
2019-05-06 17:56:06.710  INFO 16581 --- [           main] x.txns.WhenReady                         : END OF PUBLISHING
2019-05-06 17:56:06.726  INFO 16581 --- [         task-1] x.txns.FooListener                       : POST-COMMIT: Foo(id=1, key=BAR, value=3)
```

Relevant output of second run of `./gradlew bootRun`:

```
2019-05-06 17:56:58.480  INFO 16631 --- [           main] x.txns.WhenReady                         : SAVED: Foo(id=2, key=BAR, value=3)
2019-05-06 17:56:58.497  INFO 16631 --- [           main] x.txns.WhenReady                         : END OF PUBLISHING
2019-05-06 17:56:58.513  INFO 16631 --- [         task-1] x.txns.FooListener                       : POST-COMMIT: Foo(id=1, key=BAR, value=3)
2019-05-06 17:56:58.513  INFO 16631 --- [         task-2] x.txns.FooListener                       : POST-COMMIT: Foo(id=2, key=BAR, value=3)
```
