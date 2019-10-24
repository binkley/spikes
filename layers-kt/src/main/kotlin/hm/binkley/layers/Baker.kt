package hm.binkley.layers

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import javax.script.ScriptEngineManager

class Baker(val repository: String) {
    val layers = Layers()

    private val scriptsDir = Files.createTempDirectory("layers")
    private val git = Git.cloneRepository()
            .setBranchesToClone(setOf("refs/head/master"))
            .setDirectory(scriptsDir.toFile())
            .setProgressMonitor(SimpleProgressMonitor())
            .setURI(repository)
            .call()
    private val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    init {
        recursivelyDeleteOnExit(scriptsDir)
        load()
    }

    private fun load() {
        val scriptsDirFile = scriptsDir.toFile()
        scriptsDirFile.list { dir, name ->
            name.endsWith(".kts")
        }!!.sortedBy {
            it.removeSuffix(".kts").toInt()
        }.map {
            scriptsDirFile.resolve(it).readText()
        }.forEach {
            layers.new(it)
        }
    }

    fun close() = git.close()

    fun createLayer(description: String, script: String,
            notes: String? = null) {
        val trimmedScript = script.trimIndent()

        val layer = layers.new(trimmedScript)
        println("#${layer.slot} - $trimmedScript")
        println(layer.forDiff())

        layer.save(description, trimmedScript, notes)
    }

    override fun toString() =
            "${this::class.simpleName}{repository=$repository, scriptsDir=$scriptsDir, layers=$layers}"

    private fun Layers.new(script: String): Layer {
        with(engine) {
            val layer = commit()

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

    private fun Layer.save(description: String,
            trimmedScript: String, notes: String?) {
        fun Git.write(ext: String, contents: String) {
            val fileName = "$slot.$ext"
            val scriptsDirFile = scriptsDir.toFile()
            val scriptFile = File("$scriptsDirFile/$fileName")
            scriptFile.writeText(contents)
            scriptFile.appendText("\n")
            add().addFilepattern(fileName).call()
        }

        with(git) {
            write("kts", trimmedScript)
            write("txt", forDiff())
            notes?.also {
                write("notes", it)
            }

            val commit = commit()
            commit.message = description.trimIndent().trim()
            commit.call()

            git.push().call()
        }
    }

    private fun recursivelyDeleteOnExit(dir: Path) {
        Runtime.getRuntime().addShutdownHook(Thread({
            try {
                println("Deleting $dir...")
                recursivelyDelete(dir)
                println("Deleted $dir")
            } catch (e: IOException) {
                throw RuntimeException("Did not fully delete $dir: $e", e)
            }
        }, "Deleting layers temp repository: $dir"))
    }

    private fun recursivelyDelete(dir: Path) {
        walkFileTree(dir, object : SimpleFileVisitor<Path>() {
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

    private class SimpleProgressMonitor : ProgressMonitor {
        override fun start(totalTasks: Int) =
                println("Starting work on $totalTasks tasks")

        override fun beginTask(title: String, totalWork: Int) =
                println("Start $title: $totalWork")

        override fun update(completed: Int) = print("$completed-")

        override fun endTask() = println("Done")

        override fun isCancelled() = false
    }
}
