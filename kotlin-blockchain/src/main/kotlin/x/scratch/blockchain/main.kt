package x.scratch.blockchain

import java.time.Duration
import java.time.Instant
import java.time.Instant.EPOCH

fun main() {
    runTimeAndDump(
        difficulty = 0,
        genesisTimestamp = Instant.now(),
        firstBlockData = "Hello, world!"
    ) {
        Instant.now()
    }

    runTimeAndDump(
        difficulty = 4,
        genesisTimestamp = EPOCH,
        firstBlockData = mapOf("greeting" to "Hello, world!")
    ) { initialTimestamp ->
        initialTimestamp.plusMillis(1L)
    }
}

private fun Blockchain.verifyAndDump() {
    verify(Blockchain.DEFAULT_FUNCTIONS)

    println("difficulty -> $difficulty")
    println("blockchain -> $this")
    println("latest -> ${last()}")
    println("first SHA-256 by height -> ${this[0]}")
    println(
        "last SHA-256 by hash -> ${this["SHA-256", last().hashes["SHA-256"]
            ?: error("No SHA-256")]}"
    )
    println()
    println(pretty())
}

private fun <R> timing(block: () -> R): Pair<R, Duration> {
    val start = Instant.now()
    val result = block()!!
    val end = Instant.now()
    return result to Duration.between(start, end)
}

private fun runTimeAndDump(
    difficulty: Int,
    genesisTimestamp: Instant,
    firstBlockData: Any,
    firstBlockTimestamp: (Instant) -> Instant
) {
    println()
    println("========")
    println()

    var running = timing {
        Blockchain.new(
            purpose = "Example",
            difficulty = difficulty,
            genesisTimestamp = genesisTimestamp
        )
    }
    val blockchain = running.first
    println("Genesis timing -> ${running.second}")
    blockchain.verifyAndDump()

    println()

    running = timing {
        blockchain.newBlock(
            purpose = "Example",
            data = firstBlockData,
            // Add some hash functions to existing chain
            functions = setOf("MD5", "SHA-256", "SHA3-256"),
            timestamp = firstBlockTimestamp(genesisTimestamp)
        )
    }
    println("New block timing -> ${running.second}")
    blockchain.verifyAndDump()
}
