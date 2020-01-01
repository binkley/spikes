package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.time.Instant
import java.time.Instant.EPOCH

private const val genesisData = "Genesis"

fun main() {
    runVerifyAndDump(
        difficulty = 0,
        genesisTimestamp = Instant.now(),
        firstBlockData = "Hello, world!"
    ) {
        Instant.now()
    }

    runVerifyAndDump(
        difficulty = 4,
        genesisTimestamp = EPOCH,
        firstBlockData = mapOf("greeting" to "Hello, world!")
    ) { initialTimestamp ->
        initialTimestamp.plusMillis(1L)
    }
}

fun Blockchain.pretty(): String {
    val buf = StringBuilder()

    buf.append("BLOCKCHAIN\n* Genesis data: ")
    buf.append(genesisData)
    buf.append("\n* Difficulty: ")
    buf.append(difficulty)

    for (block in this.asReversed()) {
        buf.append("\n---\n")
        block.pretty(buf)
    }

    return buf.toString()
}

fun Block.pretty(buf: StringBuilder) {
    buf.append("BLOCK #")
    buf.append(height)
    buf.append(" @ ")
    buf.append(timestamp)
    buf.append("\n* Purpose: ")
    buf.append(purpose)
    buf.append("\n* Hashes:")
    hashes.forEach { (function, timedHash) ->
        buf.append("\n  - ")
        buf.append(function)
        buf.append(" (")
        buf.append(timedHash.timing)
        buf.append("): ")
        buf.append(timedHash.hash)
    }
    buf.append("\n* Previous hashes:")
    previousHashes.forEach { (function, timedHash) ->
        buf.append("\n  - ")
        buf.append(function)
        buf.append(" (")
        buf.append(timedHash.timing)
        buf.append("): ")
        buf.append(timedHash.hash)
    }
    val truncateAfter = genesisData.length
    val prettyData = data.toString()
    if (prettyData.length > truncateAfter) {
        buf.append("\n* Data (truncated): ")
        // TODO: Where's my helper function?!
        buf.append(prettyData.substring(0, truncateAfter))
        buf.append(" ...")
    } else {
        buf.append("\n* Data: ")
        buf.append(prettyData)
    }
}

private fun Blockchain.verifyAndDump() {
    verify(Blockchain.DEFAULT_FUNCTIONS)

    println()
    println(pretty())
    println()
    println("latest -> ${last()}")
    println("first SHA-256 by height -> ${this[0]}")
    println(
        "last SHA-256 by hash -> ${this["SHA-256", last().hashes["SHA-256"]?.hash
            ?: error("No SHA-256")]}"
    )
}

private fun runVerifyAndDump(
    difficulty: Int,
    genesisTimestamp: Instant,
    firstBlockData: Any,
    firstBlockTimestamp: (Instant) -> Instant
) {
    println()
    println("========")
    println()

    val blockchain = Blockchain.new(
        genesisData = genesisData,
        purpose = "Example",
        difficulty = difficulty,
        genesisTimestamp = genesisTimestamp
    )
    blockchain.verifyAndDump()

    println()

    blockchain.newBlock(
        purpose = "Example",
        data = firstBlockData,
        // Add some hash functions to existing chain
        functions = setOf("MD5", "SHA-256", "SHA3-256"),
        timestamp = firstBlockTimestamp(genesisTimestamp)
    )
    blockchain.verifyAndDump()
}
