package hm.binkley.layers

import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class LayersTest {
    @Test
    internal fun `should persist`(@TempDir baseTempDir: Path) {
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
                layer["b"] = sum(default=0)
        """
        val aNote = """
                Just a note
        """
        baker.noisyCreateLayer(description = aDescription,
                script = aRuleDefinition,
                notes = aNote)
        val bDescription = """
                You are you
        """
        val bRuleDefinition = """
                layer["b"] = 1
        """
        baker.noisyCreateLayer(description = bDescription,
                script = bRuleDefinition)

        val bakerMap = baker.layers.asMap()
        println(bakerMap)

        assert(bakerMap["a"] == true)
        assert(bakerMap["b"] == 1)

        assert(bakerMap["a"] == true)

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
        assert(cloneDir.resolve("1.kts").exists())
        assert(cloneDir.resolve("1.txt").exists())
        assert(!cloneDir.resolve("1.notes").exists())

        val nextBaker = Baker(repoDir.absolutePath)

        val nextBakerMap = nextBaker.layers.asMap()
        println(nextBakerMap)

        assert(nextBakerMap["a"] == true)
        assert(nextBaker == baker)

        nextBaker.close()
        cloneGit.close()
        repoGit.close()
    }
}
