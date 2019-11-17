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
    internal fun `should run tests`() {
        // TODO: Why isn't Maven running tests?
        if (false) fail<Nothing>("PROVE TESTS RUN")
    }

    @Test
    internal fun `should persist`(@TempDir baseTempDir: Path) {
        val repoDir = setupRepository(baseTempDir)

        val baker = PersistedLayers(
            Persistence(repoDir.absolutePath),
            Scripting()
        ).use {
            val aCommitMessage = "I am me"
            // Too much whitespace on purpose
            val aDescription = " $aCommitMessage   "
            val aRuleDefinition = """
                layer["b-enabled"] = current(default=true)
                layer["b"] = current(default=0) // Rule replaced later
            """
            val aNote = """
                Just a note
            """
            val aLayer = it.newLayer(
                description = aDescription,
                script = aRuleDefinition,
                notes = aNote
            )

            assertThat(aLayer.meta["full-message"])
                .isEqualTo(aCommitMessage)

            val bCommitMessage = """You are you"""
            val bDescription = """
                $bCommitMessage
            """
            val bRuleDefinition = """
                layer["b"] = 11
            """
            val bLayer = it.newLayer(
                description = bDescription,
                script = bRuleDefinition, notes = null
            )

            assertThat(bLayer.meta["full-message"])
                .isEqualTo(bCommitMessage)

            it.newLayer(
                description = "Empty", script = "", notes = """
                An example of marker notes
            """
            )

            it.newLayer(
                description = "We are each other", script = """
                layer["b"] = total(default=0) // Rule replaced
                layer["b-bonus"] = bonus(otherKey="b", default=0)
            """, notes = """
                A complex rule dependent on another calculation
            """
            )

            it.newLayer(
                description = "Even better!", script = """
                layer["b-bonus"] = 1
            """, notes = """
                Bump the 'b' bonus
            """
            )

            it.newLayer(
                description = "Rule of plain text", script = """
                layer["c"] = current(default=null)
            """, notes = null
            )

            it.newLayer(
                description = "Plain text", script = """
                layer["c"] = "Bob the Builder"
            """, notes = null
            )

            assertThat(it.asList()).hasSize(7)
            assertThat(it.asMap()).hasSize(4)
            assertThat(it.asMap()).containsEntry("b-enabled", true)
            assertThat(it.asMap()).containsEntry("b", 11)
            assertThat(it.asMap()).containsEntry("b-bonus", 1)
            assertThat(it.asMap()).containsEntry("c", "Bob the Builder")

            it
        }

        PersistedLayers(Persistence(repoDir.absolutePath), Scripting()).use {
            assertThat(it.asList()).isEqualTo(baker.asList())
            assertThat(it.asMap()).isEqualTo(baker.asMap())
            assertThat(it).isEqualTo(baker)
        }

        val cloneDir = setupClone(baseTempDir, repoDir)
        PersistedLayers(Persistence(cloneDir.absolutePath), Scripting()).use {
            assertThat(it.asList()).isEqualTo(baker.asList())
            assertThat(it.asMap()).isEqualTo(baker.asMap())

            assertThat(cloneDir.resolve("0.kts")).exists()
            assertThat(cloneDir.resolve("0.txt")).exists()
            assertThat(cloneDir.resolve("0.notes")).exists()
            assertThat(cloneDir.resolve("1.kts")).exists()
            assertThat(cloneDir.resolve("1.txt")).exists()
            assertThat(cloneDir.resolve("1.notes")).doesNotExist()
            assertThat(cloneDir.resolve("2.kts")).hasContent("")
            assertThat(cloneDir.resolve("2.txt")).doesNotExist()
            assertThat(cloneDir.resolve("2.notes")).exists()
        }
    }

    private fun setupRepository(baseTempDir: Path) =
        baseTempDir.toFile().resolve("git").also { repoDir ->
            Git.init()
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
                }
        }

    private fun createReadme(repoDir: File) {
        with(repoDir.resolve("README.md")) {
            writeText("# Working directory for Layers\n")
        }
    }

    private fun setupClone(baseTempDir: Path, repoDir: File) =
        baseTempDir.toFile().resolve("clone").also { cloneDir ->
            Git.cloneRepository()
                .setDirectory(cloneDir)
                .setURI(repoDir.absolutePath)
                .call()
                .close()
        }
}
