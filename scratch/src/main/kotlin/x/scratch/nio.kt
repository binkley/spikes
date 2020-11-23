package x.scratch

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.UserPrincipal
import java.util.stream.Stream


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

private val Path.exists: Boolean get() = Files.exists(this)
private val Path.lastModifiedTime: FileTime
    get() = Files.getLastModifiedTime(this)
private val Path.owner: UserPrincipal get() = Files.getOwner(this)
private val Path.posixFilePermissions: Set<PosixFilePermission>
    get() = Files.getPosixFilePermissions(this)

private fun Path.list(): Stream<Path> = Files.list(this)
