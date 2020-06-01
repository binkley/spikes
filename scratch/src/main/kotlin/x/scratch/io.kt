package x.scratch

import java.nio.file.Files
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

val Path.exists get() = Files.exists(this)
val Path.lastModifiedTime get() = Files.getLastModifiedTime(this)
val Path.owner get() = Files.getOwner(this)
val Path.posixFilePermissions get() = Files.getPosixFilePermissions(this)
fun Path.list() = Files.list(this)
