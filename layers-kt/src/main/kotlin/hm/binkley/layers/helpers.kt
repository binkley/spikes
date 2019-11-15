package hm.binkley.layers

import java.io.IOException
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

internal fun Int.toIsoDateTime() =
    ISO_DATE_TIME.withZone(UTC).format(Instant.ofEpochMilli(this * 1000L))

internal fun Path.deleteRecursively() {
    walkFileTree(this, object : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(file: Path, attrs: BasicFileAttributes) =
            file.delete()

        @Throws(IOException::class)
        override fun postVisitDirectory(dir: Path, e: IOException?) =
            if (null == e) dir.delete() else throw e

        private fun Path.delete() = Files.delete(this).let { CONTINUE }
    })
}

internal fun String.clean() = this.trimIndent().trim()
