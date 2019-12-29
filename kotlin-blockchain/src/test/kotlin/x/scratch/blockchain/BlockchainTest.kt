package x.scratch.blockchain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant.EPOCH

internal class BlockchainTest {
    @Test
    fun `should hash first block`() {
        val blockchain = Blockchain.new(timestamp = EPOCH)

        assertEquals(
            "d7cce22abb7945c814718fb71c5a2d27f2da47a39a01aee14e5b2a1cddb9bdd9",
            blockchain[0].hash
        )
    }
}
