# Gradle test sets

A demo multi-module project for
[Gradle test sets](https://github.com/unbroken-dome/gradle-testsets-plugin)

## Usage

```
./gradlew --no-build-cache build taskTree
```

After building at least once, you can see the impact of the build cache:

```
./gradlew build taskTree
```

[Gradle task tree](https://github.com/dorongold/gradle-task-tree) pretty
prints the tasks that would run.

## Structure

* `[ab]/src/main/*` - Production code and resources
* `[ab]/src/test/*` - Unit test code and resources
* `[ab]/src/integrationTest/*` - Integration test code and resources

Module `a` depends on module `b`.
