package x.scratch.blockchain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import x.scratch.blockchain.Blockchain.Companion.DEFAULT_FUNCTIONS
import java.time.Instant.EPOCH

internal class BlockchainTest {
    @Test
    fun `should hash first block`() {
        val blockchain = Blockchain.new(
            purpose = "Testing",
            genesisTimestamp = EPOCH
        )

        blockchain.check(DEFAULT_FUNCTIONS)

        assertEquals(
            "fd4d9c45f4305ce40fd2ce6d5866eff8af45cbb358e23e780745db58c4e17661",
            blockchain[0].hashes["SHA-256"]
        )
    }

    @Test
    fun `should hash second block`() {
        val blockchain = Blockchain.new(
            purpose = "Testing",
            genesisTimestamp = EPOCH
        )

        blockchain.newBlock(
            purpose = "Testing",
            data = "FRODO LIVES!",
            timestamp = blockchain[0].timestamp.plusMillis(1L)
        )

        blockchain.check(DEFAULT_FUNCTIONS)

        assertEquals(
            "a7c3ea75ae0c9e865116939dbad61b43f874ec62cfe962b51be2ed8a0490a010",
            blockchain[1].hashes["SHA-256"]
        )
    }

    @Test
    fun `should have independent hash functions`() {
        val functions = setOf("MD5", "SHA-256")
        val blockchain = Blockchain.new(
            purpose = "Testing",
            initialFunctions = functions,
            genesisTimestamp = EPOCH
        )

        blockchain.check(functions)

        assertEquals(
            "cf48b3eccbe0d3ecbaf4b25d370a2fcc",
            blockchain[0].hashes["MD5"]
        )
        assertEquals(
            "fd4d9c45f4305ce40fd2ce6d5866eff8af45cbb358e23e780745db58c4e17661",
            blockchain[0].hashes["SHA-256"]
        )
    }

    @Test
    fun `should drop obsolete hash functions`() {
        val functions = setOf("MD5", "SHA-256")
        val blockchain = Blockchain.new(
            purpose = "Testing",
            initialFunctions = functions,
            genesisTimestamp = EPOCH
        )

        blockchain.newBlock(
            data = "FRODO LIVES!",
            purpose = "Testing",
            functions = setOf("SHA-256"),
            timestamp = blockchain[0].timestamp.plusMillis(1L)
        )

        blockchain.check(functions)

        assertNull(blockchain[1].hashes["MD5"])
        assertEquals(
            "a7c3ea75ae0c9e865116939dbad61b43f874ec62cfe962b51be2ed8a0490a010",
            blockchain[1].hashes["SHA-256"]
        )
    }
}
