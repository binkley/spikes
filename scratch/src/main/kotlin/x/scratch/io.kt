package x.scratch

import java.nio.file.Path

fun main() {
    println("PATHS API")
    val etc = Path.of("/etc")
    println("/etc -> $etc")
    println(etc.exists)
    println(etc.lastModifiedTime)
    println(etc.owner)
    println(etc.posixFilePermissions)
    etc.list().use {
        println(it.findFirst().get())
    }
}
