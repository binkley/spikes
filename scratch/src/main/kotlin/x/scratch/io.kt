package x.scratch

import java.nio.file.Path

fun main() {
    println("== PATHS API")
    val etc = Path.of("/etc")
    println("PATH -> $etc")
    println("EXISTS -> ${etc.exists}")
    println("LAST-MODIFIED-TIME -> ${etc.lastModifiedTime}")
    println("OWNER -> ${etc.owner}")
    println("POSIX-FILE-PERMISSIONS -> ${etc.posixFilePermissions}")
    etc.list().use {
        println("LIST FIND-FIRST -> ${it.findFirst().get()}")
    }
}
