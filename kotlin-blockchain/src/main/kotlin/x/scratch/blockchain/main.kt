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
    hashes.pretty(buf)
    buf.append("\n* Previous hashes:")
    previousHashes.pretty(buf)
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

fun Map<String, TimedHash>.pretty(buf: StringBuilder) {
    forEach { (function, timedHash) ->
        buf.append("\n  - ")
        buf.append(function)
        buf.append(" (")
        buf.append(timedHash.nonce)
        buf.append(" runs @")
        buf.append(timedHash.timing)
        buf.append("): ")
        buf.append(timedHash.hash)
    }
}

private fun runVerifyAndDump(
    difficulty: Int,
    genesisTimestamp: Instant,
    firstBlockData: Any,
    computeFirstBlockTimestamp: (Instant) -> Instant
) {
    val blockchain = Blockchain.new(
        genesisData = genesisData,
        purpose = "Example",
        difficulty = difficulty,
        genesisTimestamp = genesisTimestamp
    )
    println()
    println("========")
    println(blockchain.pretty())

    println()

    val firstBlockTimestamp = computeFirstBlockTimestamp(genesisTimestamp)

    blockchain.newBlock(
        data = firstBlockData,
        purpose = "Example",
        // Add some hash functions to existing chain
        functions = setOf("MD5", "SHA-256", "SHA3-256"),
        timestamp = firstBlockTimestamp
    )
    println()
    println("========")
    println(blockchain.pretty())

    blockchain.handOffBlock(
        data = "Stick em' up!  This is a hand off!",
        purpose = "Example",
        previousHash = "SHA-256",
        nextHash = "SHA3-256",
        timestamp = firstBlockTimestamp.plusMillis(1L)
    )
    println()
    println("========")
    println(blockchain.pretty())
}
