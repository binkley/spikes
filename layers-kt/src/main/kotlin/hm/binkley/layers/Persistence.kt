package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.Objects

class Persistence(private val repository: String) : AutoCloseable {
    private val scriptsDir =
        Files.createTempDirectory(
            "layers"
        )
    private val git = Git.cloneRepository()
        .setDirectory(scriptsDir.toFile())
        .setURI(repository)
        .call()

    override fun close() {
        git.close()
        scriptsDir.recursivelyDelete()
    }

    internal fun refresh(size: Int, new: (scriptFile: String) -> Unit) =
        scriptsDir.load(size, new)

    internal fun <R> letGit(block: (Git) -> R): R = git.let(block)

    internal fun scriptFile(fileName: String): File {
        val scriptsDirFile = scriptsDir.toFile()
        return File("$scriptsDirFile/$fileName")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Persistence

        return repository == other.repository
    }

    override fun hashCode() =
        Objects.hash(repository)

    override fun toString() =
        "${this::class.simpleName}{repository=$repository, scriptsDir=$scriptsDir}"

    private fun Path.load(size: Int, new: (scriptFile: String) -> Unit) {
        val scriptsDirFile = toFile()
        val scripts = scriptsDirFile.list { _, name ->
            name.endsWith(".kts")
        }!!.sortedBy {
            it.removeSuffix(".kts").toInt()
        }

        scripts.subList(size, scripts.size).map {
            scriptsDirFile.resolve(it).readText().clean()
        }.forEach {
            new(it)
        }
    }
}
