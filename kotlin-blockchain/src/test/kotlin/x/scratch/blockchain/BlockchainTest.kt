package x.scratch.blockchain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant.EPOCH

internal class BlockchainTest {
    @Test
    fun `should hash first block`() {
        val blockchain = Blockchain.new(timestamp = EPOCH)

        blockchain.check()

        assertEquals(
            "d7cce22abb7945c814718fb71c5a2d27f2da47a39a01aee14e5b2a1cddb9bdd9",
            blockchain[0].hashes["SHA-256"]
        )
    }

    @Test
    fun `should hash second block`() {
        val blockchain = Blockchain.new(timestamp = EPOCH)

        blockchain.newBlock(
            data = "FRODO LIVES!",
            timestamp = blockchain[0].timestamp.plusMillis(1L)
        )

        blockchain.check()

        assertEquals(
            "f0d286cf862f2996442ef74bf49772a7c5f01c1680f8b4f1c1634f5222c80d8c",
            blockchain[1].hashes["SHA-256"]
        )
    }
}
