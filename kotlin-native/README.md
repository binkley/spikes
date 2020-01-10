# Kotlin native spike

- Linux, MacOS
- [_A Basic Kotlin/Native Application_](https://kotlinlang.org/docs/tutorials/native/basic-kotlin-native-app.html)
- Gradle 6.0.1
- Kotlin 1.3.61

## TODOs

* Follow up on (_Unable to access kotlin.native.Platform_)[https://github.com/JetBrains/kotlin-native/issues/3680]

## Gotchas

* On Linux, install `libncurses5`, else there is a link error for
  `libtinfo.so.5`.  See
  [libtinfo.so.5: cannot open shared object file: No such file or directory](https://github.com/msink/kotlin-libui/issues/27).
* Setting up `build.gradle.kts` for multiple native platforms is a challenge.
  See [_Building Multiplatform Projects with Gradle_](https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#adding-dependencies)
  for hints.

## Warts

* Gradle has an annoying 3-file solution for keeping plugin and dependency
  versions in one place.  See
  [_Plugin Version Management_](https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_version_management).
* This is _not_ a multi-platform build in that it will not build MacOS on a
  Linux box, etc.  It does however not complain if you simply build on Linux,
  and are happy with getting only a Linux binary.
* Demonstrate example Kotlin/Native limitation from
  [_Why the Kotlin/Native memory model cannot hold._](https://itnext.io/why-the-kotlin-native-memory-model-cannot-hold-ae1631d80cf6)
  (See [this commit](https://github.com/binkley/spikes/commit/77e39d4c09592eb868c98a9ad059522fa6fa2c58)).

## It works

Run `./build/bin/linuxX64/releaseExecutable/hello-from-kotlin-native`:

```
Hello Kotlin/Native!
```

Using `strace` to see what system calls the binary makes:

```
execve("./build/bin/linuxX64/releaseExecutable/hello-from-kotlin-native.kexe", ["./build/bin/linuxX64/releaseExecut"...], 0x7ffee970b690 /* 55 vars */) = 0
brk(NULL)                               = 0x3b8000
arch_prctl(0x3001 /* ARCH_??? */, 0x7fff714a3530) = -1 EINVAL (Invalid argument)
access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
openat(AT_FDCWD, "/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
fstat(3, {st_mode=S_IFREG|0644, st_size=129302, ...}) = 0
mmap(NULL, 129302, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f43b79d3000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libdl.so.2", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\0\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0 \22\0\0\0\0\0\0"..., 832) = 832
fstat(3, {st_mode=S_IFREG|0644, st_size=18816, ...}) = 0
mmap(NULL, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f43b79d1000
mmap(NULL, 20752, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f43b79cb000
mmap(0x7f43b79cc000, 8192, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1000) = 0x7f43b79cc000
mmap(0x7f43b79ce000, 4096, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x3000) = 0x7f43b79ce000
mmap(0x7f43b79cf000, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x3000) = 0x7f43b79cf000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libm.so.6", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0\300\363\0\0\0\0\0\0"..., 832) = 832
fstat(3, {st_mode=S_IFREG|0644, st_size=1369352, ...}) = 0
mmap(NULL, 1368336, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f43b787c000
mmap(0x7f43b788b000, 684032, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0xf000) = 0x7f43b788b000
mmap(0x7f43b7932000, 618496, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0xb6000) = 0x7f43b7932000
mmap(0x7f43b79c9000, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x14c000) = 0x7f43b79c9000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libpthread.so.0", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\0\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0\360\201\0\0\0\0\0\0"..., 832) = 832
lseek(3, 824, SEEK_SET)                 = 824
read(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\09V\4W\221\35\226\215\236\6\10\215\240\25\227\v"..., 68) = 68
fstat(3, {st_mode=S_IFREG|0755, st_size=158288, ...}) = 0
lseek(3, 824, SEEK_SET)                 = 824
read(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\09V\4W\221\35\226\215\236\6\10\215\240\25\227\v"..., 68) = 68
mmap(NULL, 140448, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f43b7859000
mmap(0x7f43b7860000, 69632, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x7000) = 0x7f43b7860000
mmap(0x7f43b7871000, 20480, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x18000) = 0x7f43b7871000
mmap(0x7f43b7876000, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c000) = 0x7f43b7876000
mmap(0x7f43b7878000, 13472, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f43b7878000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libgcc_s.so.1", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\0\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0\3405\0\0\0\0\0\0"..., 832) = 832
fstat(3, {st_mode=S_IFREG|0644, st_size=100888, ...}) = 0
mmap(NULL, 103504, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f43b783f000
mmap(0x7f43b7842000, 69632, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x3000) = 0x7f43b7842000
mmap(0x7f43b7853000, 16384, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x14000) = 0x7f43b7853000
mmap(0x7f43b7857000, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x17000) = 0x7f43b7857000
close(3)                                = 0
openat(AT_FDCWD, "/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0\360r\2\0\0\0\0\0"..., 832) = 832
lseek(3, 64, SEEK_SET)                  = 64
read(3, "\6\0\0\0\4\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0"..., 784) = 784
lseek(3, 848, SEEK_SET)                 = 848
read(3, "\4\0\0\0\20\0\0\0\5\0\0\0GNU\0\2\0\0\300\4\0\0\0\3\0\0\0\0\0\0\0", 32) = 32
lseek(3, 880, SEEK_SET)                 = 880
read(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\0!U\364U\255V\275\207\34\202%\274\312\205\356%"..., 68) = 68
fstat(3, {st_mode=S_IFREG|0755, st_size=2025032, ...}) = 0
lseek(3, 64, SEEK_SET)                  = 64
read(3, "\6\0\0\0\4\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0@\0\0\0\0\0\0\0"..., 784) = 784
lseek(3, 848, SEEK_SET)                 = 848
read(3, "\4\0\0\0\20\0\0\0\5\0\0\0GNU\0\2\0\0\300\4\0\0\0\3\0\0\0\0\0\0\0", 32) = 32
lseek(3, 880, SEEK_SET)                 = 880
read(3, "\4\0\0\0\24\0\0\0\3\0\0\0GNU\0!U\364U\255V\275\207\34\202%\274\312\205\356%"..., 68) = 68
mmap(NULL, 2032984, PROT_READ, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f43b764e000
mmap(0x7f43b7673000, 1540096, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x25000) = 0x7f43b7673000
mmap(0x7f43b77eb000, 303104, PROT_READ, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x19d000) = 0x7f43b77eb000
mmap(0x7f43b7835000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1e6000) = 0x7f43b7835000
mmap(0x7f43b783b000, 13656, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f43b783b000
close(3)                                = 0
mmap(NULL, 8192, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f43b764c000
arch_prctl(ARCH_SET_FS, 0x7f43b764d400) = 0
mprotect(0x7f43b7835000, 12288, PROT_READ) = 0
mprotect(0x7f43b7857000, 4096, PROT_READ) = 0
mprotect(0x7f43b7876000, 4096, PROT_READ) = 0
mprotect(0x7f43b79c9000, 4096, PROT_READ) = 0
mprotect(0x7f43b79cf000, 4096, PROT_READ) = 0
mprotect(0x229000, 36864, PROT_READ)    = 0
mprotect(0x7f43b7a1f000, 4096, PROT_READ) = 0
munmap(0x7f43b79d3000, 129302)          = 0
set_tid_address(0x7f43b764d6d0)         = 20010
set_robust_list(0x7f43b764d6e0, 24)     = 0
rt_sigaction(SIGRTMIN, {sa_handler=0x7f43b7860c50, sa_mask=[], sa_flags=SA_RESTORER|SA_SIGINFO, sa_restorer=0x7f43b786e540}, NULL, 8) = 0
rt_sigaction(SIGRT_1, {sa_handler=0x7f43b7860cf0, sa_mask=[], sa_flags=SA_RESTORER|SA_RESTART|SA_SIGINFO, sa_restorer=0x7f43b786e540}, NULL, 8) = 0
rt_sigprocmask(SIG_UNBLOCK, [RTMIN RT_1], NULL, 8) = 0
prlimit64(0, RLIMIT_STACK, NULL, {rlim_cur=8192*1024, rlim_max=RLIM64_INFINITY}) = 0
brk(NULL)                               = 0x3b8000
brk(0x3d9000)                           = 0x3d9000
futex(0x2320e0, FUTEX_WAKE_PRIVATE, 2147483647) = 0
write(1, "Hello Kotlin/Native!", 20)    = 20
write(1, "\n", 1)                       = 1
clock_gettime(CLOCK_MONOTONIC, {tv_sec=51397, tv_nsec=956979420}) = 0
clock_gettime(CLOCK_MONOTONIC, {tv_sec=51397, tv_nsec=956998429}) = 0
exit_group(0)                           = ?
+++ exited with 0 +++
```
