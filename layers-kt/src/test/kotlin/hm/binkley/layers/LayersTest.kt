package hm.binkley.layers

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class LayersTest {
    @Test
    internal fun `should persist`(@TempDir baseTempDir: Path) {
        if (false) Assertions.fail<Nothing>("PROVE TESTS RUN")

        val repoDir = baseTempDir.toFile().resolve("git")
        val repoGit = Git.init()
                .setDirectory(repoDir)
                .setBare(true)
                .call()
        repoGit.close()

        val baker = Baker(repoDir.absolutePath)

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

        val bakerMap = baker.layers.asMap()

        assert(bakerMap["a"] == true)
        assert(bakerMap["b"] == 1)

        baker.close()

        val nextBaker = Baker(repoDir.absolutePath)
        assert(nextBaker == baker)
        nextBaker.close()

        val cloneDir = baseTempDir.toFile().resolve("clone")
        val cloneGit = Git.cloneRepository()
                .setBranch("master")
                .setBranchesToClone(setOf("refs/head/master"))
                .setDirectory(cloneDir)
                .setURI(repoDir.absolutePath)
                .call()
        val cloneBaker = Baker(cloneDir.absolutePath)

        assert(cloneBaker.layers.asMap() == baker.layers.asMap())

        assert(cloneDir.resolve("0.kts").exists())
        assert(cloneDir.resolve("0.txt").exists())
        assert(cloneDir.resolve("0.notes").exists())
        assert(cloneDir.resolve("1.kts").exists())
        assert(cloneDir.resolve("1.txt").exists())
        assert(!cloneDir.resolve("1.notes").exists())

        cloneBaker.close()
        cloneGit.close()
    }
}
