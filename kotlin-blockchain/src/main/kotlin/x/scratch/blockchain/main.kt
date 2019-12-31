package x.scratch.blockchain

import java.time.Duration
import java.time.Instant
import java.time.Instant.EPOCH

fun main() {
    runTimeAndDump(
        difficulty = 0,
        initialTimestamp = Instant.now(),
        firstBlockData = "Hello, world!"
    ) {
        Instant.now()
    }

    runTimeAndDump(
        difficulty = 4,
        initialTimestamp = EPOCH,
        firstBlockData = mapOf("greeting" to "Hello, world!")
    ) { initialTimestamp ->
        initialTimestamp.plusMillis(1L)
    }
}

private fun Blockchain.checkAndDump() {
    check(Blockchain.DEFAULT_FUNCTIONS)

    println("difficulty -> $difficulty")
    println("blockchain -> $this")
    println("latest -> ${last()}")
    println("first genesis -> ${first().genesis}")
    println("last genesis -> ${last().genesis}")
    println("first SHA-256 by height -> ${this[0]}")
    println(
        "last SHA-256 by hash -> ${this["SHA-256", last().hashes["SHA-256"]
            ?: error("No SHA-256")]}"
    )

    for (block in this) println("block#${block.height} -> $block")
}

private fun <R> timing(block: () -> R): Pair<R, Duration> {
    val start = Instant.now()
    val result = block()!!
    val end = Instant.now()
    return result to Duration.between(start, end)
}

private fun runTimeAndDump(
    difficulty: Int,
    initialTimestamp: Instant,
    firstBlockData: Any,
    firstBlockTimestamp: (Instant) -> Instant
) {
    println()
    println("========")
    println()

    var running = timing {
        Blockchain.new(
            difficulty = difficulty,
            timestamp = initialTimestamp
        )
    }
    val blockchain = running.first
    println("Genesis timing -> ${running.second}")
    blockchain.checkAndDump()

    println()

    running = timing {
        blockchain.newBlock(
            data = firstBlockData,
            timestamp = firstBlockTimestamp(initialTimestamp)
        )
    }
    println("New block timing -> ${running.second}")
    blockchain.checkAndDump()
}
