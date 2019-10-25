package hm.binkley.layers

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class LayersTest {
    fun Baker.myCreateLayer(description: String, script: String,
            notes: String? = null) {
        val layer = createLayer(description, script, notes)
        println("#${layer.slot} - ${layer.script}")
        println(layer.forDiff())
    }

    @Test
    fun `should persist`(@TempDir baseTempDir: Path) {
        val repoDir = baseTempDir.toFile().resolve("git")
        val cloneDir = baseTempDir.toFile().resolve("clone")
        val repoGit = Git.init()
                .setDirectory(repoDir)
                .setBare(true)
                .call()
        val baker = Baker(repoDir.absolutePath)

        val aDescription = """
                I am me
        """
        val aRuleDefinition = """
                layer["a"] = last(default=true)
        """
        val aNote = """
                Just a note
        """
        baker.myCreateLayer(description = aDescription,
                script = aRuleDefinition,
                notes = aNote)

        baker.close()

        val cloneGit = Git.cloneRepository()
                .setBranch("master")
                .setBranchesToClone(setOf("refs/head/master"))
                .setDirectory(cloneDir)
                .setURI(repoDir.absolutePath)
                .call()

        assert(cloneDir.resolve("0.kts").exists())
        assert(cloneDir.resolve("0.txt").exists())
        assert(cloneDir.resolve("0.notes").exists())

        val nextBaker = Baker(repoDir.absolutePath)

        assert(nextBaker == baker)

        nextBaker.close()
        cloneGit.close()
        repoGit.close()
    }
}
