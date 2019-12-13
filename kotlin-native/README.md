# Kotlin native spike

- Linux
- [A Basic Kotlin/Native Application](https://kotlinlang.org/docs/tutorials/native/basic-kotlin-native-app.html)
- Gradle 6.0.1
- Kotlin 1.3.61

## Gotchas

* Install `libncurses5`, else there is a link error for `libtinfo.so.5`.  See
  [libtinfo.so.5: cannot open shared object file: No such file or directory](https://github.com/msink/kotlin-libui/issues/27)

## It works

Run `./build/bin/native/releaseExecutable/kotlin-native.kexe`:

```
Hello Kotlin/Native!
```
