package hm.binkley.layers

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal fun Int.toIsoDateTime() = DateTimeFormatter.ISO_DATE_TIME.withZone(
        ZoneOffset.UTC).format(
        Instant.ofEpochMilli(this * 1000L))

internal fun Path.recursivelyDelete() {
    Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(
                file: Path, attrs: BasicFileAttributes)
                : FileVisitResult {
            Files.delete(file)
            return CONTINUE
        }

        @Throws(IOException::class)
        override fun postVisitDirectory(dir: Path,
                e: IOException?): FileVisitResult {
            if (null == e) {
                Files.delete(dir)
                return CONTINUE
            } else throw e
        }
    })
}

internal fun String.clean() = this.trimIndent().trim()
