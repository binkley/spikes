package hm.binkley.layers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class LayersTest {
    @Test
    internal fun `should persist`(@TempDir baseTempDir: Path) {
        if (false) fail<Nothing>("PROVE TESTS RUN")

        val repoDir = baseTempDir.toFile().resolve("git")
        val repoGit = Git.init()
                .setDirectory(repoDir)
                .setBare(true)
                .call()
        repoGit.close()

        val baker = PersistedLayers(repoDir.absolutePath)

        val aDescription = """
                I am me
        """
        val aRuleDefinition = """
                layer["a"] = last(default=true)
                layer["b"] = sum(default=0)
        """
        val aNote = """
                Just a note
        """
        baker.createLayer(description = aDescription,
                script = aRuleDefinition,
                notes = aNote)
        val bDescription = """
                You are you
        """
        val bRuleDefinition = """
                layer["b"] = 1
        """
        baker.createLayer(description = bDescription,
                script = bRuleDefinition)

        baker.createLayer(description = "Empty", script = "", notes = """
            An example of marker notes
        """)

        assertThat(baker.asList()).hasSize(3)
        assertThat(baker.asMap()).hasSize(2)
        assertThat(baker.asMap()).containsEntry("a", true)
        assertThat(baker.asMap()).containsEntry("b", 1)

        baker.close()

        val nextBaker = PersistedLayers(repoDir.absolutePath)
        assertThat(nextBaker).isEqualTo(baker)
        nextBaker.close()

        val cloneDir = baseTempDir.toFile().resolve("clone")
        val cloneGit = Git.cloneRepository()
                .setBranch("master")
                .setBranchesToClone(setOf("refs/head/master"))
                .setDirectory(cloneDir)
                .setURI(repoDir.absolutePath)
                .call()
        val cloneBaker = PersistedLayers(cloneDir.absolutePath)

        assertThat(cloneBaker.asList()).isEqualTo(baker.asList())
        assertThat(cloneBaker.asMap()).isEqualTo(baker.asMap())

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

        cloneBaker.close()
        cloneGit.close()
    }
}
