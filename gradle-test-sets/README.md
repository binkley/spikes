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
