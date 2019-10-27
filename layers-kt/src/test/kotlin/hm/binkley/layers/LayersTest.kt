package hm.binkley.layers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class LayersTest {
    @Test
    internal fun `should persist`(@TempDir baseTempDir: Path) {
        if (false) fail<Nothing>("PROVE TESTS RUN")

        val repoDir = setupRepository(baseTempDir)

        val baker = PersistedLayers(repoDir.absolutePath).use {
            val aCommitMessage = "I am me"
            val aDescription =
                    " $aCommitMessage   " // Too much whitespace on purpose
            val aRuleDefinition = """
                layer["a"] = last(default=true)
                layer["b"] = sum(default=0)
            """
            val aNote = """
                Just a note
            """
            val aLayer = it.createLayer(description = aDescription,
                    script = aRuleDefinition,
                    notes = aNote)

            assertThat(aLayer.meta["full-message"])
                    .isEqualTo(aCommitMessage)

            val bCommitMessage = """You are you"""
            val bDescription = """
                $bCommitMessage
            """
            val bRuleDefinition = """
                layer["b"] = 1
            """
            val bLayer = it.createLayer(description = bDescription,
                    script = bRuleDefinition)

            assertThat(bLayer.meta["full-message"])
                    .isEqualTo(bCommitMessage)

            it.createLayer(description = "Empty", script = "", notes = """
                An example of marker notes
            """)

            assertThat(it.asList()).hasSize(3)
            assertThat(it.asMap()).hasSize(2)
            assertThat(it.asMap()).containsEntry("a", true)
            assertThat(it.asMap()).containsEntry("b", 1)

            it.refresh() // Should do nothing
            assertThat(it.asList()).hasSize(3)

            it
        }

        PersistedLayers(repoDir.absolutePath).use {
            assertThat(it).isEqualTo(baker)
        }

        val cloneDir = setupClone(baseTempDir, repoDir)
        PersistedLayers(cloneDir.absolutePath).use {
            assertThat(it.asList()).isEqualTo(baker.asList())
            assertThat(it.asMap()).isEqualTo(baker.asMap())

            assertThat(cloneDir.resolve("0.kts")).exists()
            assertThat(cloneDir.resolve("0.txt")).exists()
            assertThat(cloneDir.resolve("0.notes")).exists()
            assertThat(cloneDir.resolve("1.kts")).exists()
            assertThat(cloneDir.resolve("1.txt")).exists()
            assertThat(cloneDir.resolve("1.notes")).doesNotExist()
            assertThat(cloneDir.resolve("2.kts")).exists()
            assertThat(cloneDir.resolve("2.kts")).hasContent("")
            assertThat(cloneDir.resolve("2.txt")).doesNotExist()
            assertThat(cloneDir.resolve("2.notes")).exists()
        }
    }

    private fun setupRepository(baseTempDir: Path): File {
        val repoDir = baseTempDir.toFile().resolve("git")
        return Git.init()
                .setDirectory(repoDir)
                .call().use {
                    createReadme(repoDir)
                    it.add()
                            .addFilepattern(".")
                            .call()
                    it.commit()
                            .setAllowEmpty(true)
                            .setMessage("Init")
                            .call()
                    return repoDir
                }
    }

    private fun createReadme(repoDir: File) {
        with(repoDir.resolve("README.md")) {
            writeText("# Working directory for Layers\n")
        }
    }

    private fun setupClone(baseTempDir: Path,
            repoDir: File): File {
        val cloneDir = baseTempDir.toFile().resolve("clone")
        return Git.cloneRepository()
                .setDirectory(cloneDir)
                .setURI(repoDir.absolutePath)
                .call().use {
                    return cloneDir
                }
    }
}
