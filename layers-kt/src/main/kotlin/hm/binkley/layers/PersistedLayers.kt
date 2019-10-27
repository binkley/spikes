package hm.binkley.layers

import org.eclipse.jgit.api.Git
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.Objects
import javax.script.ScriptEngineManager

class PersistedLayers(private val repository: String)
    : Layers,
        AutoCloseable {
    private val layers = MutableLayers()
    private val scriptsDir = Files.createTempDirectory("layers")
    private val git = Git.cloneRepository()
            .setDirectory(scriptsDir.toFile())
            .setURI(repository)
            .call()
    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    init {
        refresh()
    }

    override fun asList() = layers.asList()

    override fun asMap() = layers.asMap()

    override fun close() {
        git.close()
        scriptsDir.recursivelyDelete()
    }

    fun refresh() = scriptsDir.load()

    fun createLayer(description: String, script: String,
            notes: String? = null): XLayer {
        val cleanDescription = description.clean()
        val cleanScript = script.clean()
        val layer = layers.new(cleanScript)

        val scriptFile = layer.save(
                cleanDescription, cleanScript, notes?.trimIndent())

        return layer.addMetaFor(scriptFile)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedLayers

        return repository == other.repository
                && layers == other.layers
    }

    override fun hashCode() = Objects.hash(repository, layers)

    override fun toString() =
            "${this::class.simpleName}{repository=$repository, scriptsDir=$scriptsDir, layers=$layers}"

    private fun MutableLayers.new(script: String): Layer {
        with(engine) {
            val layer = commit(script)

            layer.edit {
                eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*

                    $script
                """, createBindings().apply {
                    this["layer"] = this@edit
                })
            }

            return layer
        }
    }

    private fun Path.load() {
        val scriptsDirFile = toFile()
        val scripts = scriptsDirFile.list { _, name ->
            name.endsWith(".kts")
        }!!.sortedBy {
            it.removeSuffix(".kts").toInt()
        }

        scripts.subList(asList().size, scripts.size).map {
            scriptsDirFile.resolve(it).readText().trim()
        }.forEach {
            layers.new(it)
        }
    }

    private fun Layer.save(description: String,
            trimmedScript: String, notes: String?): String {
        fun Git.write(ext: String, contents: String) {
            val fileName = "$slot.$ext"
            val scriptsDirFile = scriptsDir.toFile()
            val scriptFile = File("$scriptsDirFile/$fileName")
            scriptFile.writeText(contents)
            if (contents.isNotEmpty())
                scriptFile.appendText("\n")
            add().addFilepattern(fileName).call()
        }

        with(git) {
            write("kts", trimmedScript)
            val diff = toDiff()
            if (diff.isNotEmpty())
                write("txt", diff)
            notes?.also {
                write("notes", it)
            }

            val commit = commit()
            commit.message = description.trimIndent().trim()
            commit.call()

            git.push().call()

            return "$slot.kts"
        }
    }

    private fun Layer.addMetaFor(scriptFile: String) = apply {
        git.log().addPath(scriptFile).call().first().also {
            meta["commit-time"] = it.commitTime.toIsoDateTime()
            meta["full-message"] = it.fullMessage
        }
    }
}

private fun Int.toIsoDateTime() = ISO_DATE_TIME.withZone(UTC).format(
        Instant.ofEpochMilli(this * 1000L))

private fun Path.recursivelyDelete() {
    walkFileTree(this, object : SimpleFileVisitor<Path>() {
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

private fun String.clean() = this.trimIndent().trim()
