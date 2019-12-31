package x.scratch.blockchain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import x.scratch.blockchain.Blockchain.Companion.DEFAULT_FUNCTIONS
import java.time.Instant.EPOCH

internal class BlockchainTest {
    @Test
    fun `should hash first block`() {
        val blockchain = Blockchain.new(genesisTimestamp = EPOCH)

        blockchain.check(DEFAULT_FUNCTIONS)

        assertEquals(
            "d7cce22abb7945c814718fb71c5a2d27f2da47a39a01aee14e5b2a1cddb9bdd9",
            blockchain[0].hashes["SHA-256"]
        )
    }

    @Test
    fun `should hash second block`() {
        val blockchain = Blockchain.new(genesisTimestamp = EPOCH)

        blockchain.newBlock(
            data = "FRODO LIVES!",
            timestamp = blockchain[0].timestamp.plusMillis(1L)
        )

        blockchain.check(DEFAULT_FUNCTIONS)

        assertEquals(
            "f0d286cf862f2996442ef74bf49772a7c5f01c1680f8b4f1c1634f5222c80d8c",
            blockchain[1].hashes["SHA-256"]
        )
    }

    @Test
    fun `should have independent hash functions`() {
        val functions = setOf("MD5", "SHA-256")
        val blockchain = Blockchain.new(
            initialFunctions = functions,
            genesisTimestamp = EPOCH
        )

        blockchain.check(functions)

        assertEquals(
            "b3bc804211d93a6312c84c74b90cc64b",
            blockchain[0].hashes["MD5"]
        )
        assertEquals(
            "d7cce22abb7945c814718fb71c5a2d27f2da47a39a01aee14e5b2a1cddb9bdd9",
            blockchain[0].hashes["SHA-256"]
        )
    }

    @Test
    fun `should drop obsolete hash functions`() {
        val functions = setOf("MD5", "SHA-256")
        val blockchain = Blockchain.new(
            initialFunctions = functions,
            genesisTimestamp = EPOCH
        )

        blockchain.newBlock(
            data = "FRODO LIVES!",
            functions = setOf("SHA-256"),
            timestamp = blockchain[0].timestamp.plusMillis(1L)
        )

        blockchain.check(functions)

        assertNull(blockchain[1].hashes["MD5"])
        assertEquals(
            "f0d286cf862f2996442ef74bf49772a7c5f01c1680f8b4f1c1634f5222c80d8c",
            blockchain[1].hashes["SHA-256"]
        )
    }
}
