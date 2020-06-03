package x.scratch

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.UserPrincipal
import java.util.stream.Stream

val Path.exists: Boolean get() = Files.exists(this)
val Path.lastModifiedTime: FileTime get() = Files.getLastModifiedTime(this)
val Path.owner: UserPrincipal get() = Files.getOwner(this)
val Path.posixFilePermissions: Set<PosixFilePermission>
    get() = Files.getPosixFilePermissions(
        this
    )

fun Path.list(): Stream<Path> = Files.list(this)
