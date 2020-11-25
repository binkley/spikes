package x.scratch

import java.nio.file.Path
import kotlin.io.path.*
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
fun main() {
    println("==PATH")

    val baseDir = Path("/")
    println("✓ BASE -> $baseDir; exists? ${baseDir.exists()}")
    val subDir = baseDir / "bob"
    println("✗ SUB -> $subDir -> ${baseDir.exists()}")
}
