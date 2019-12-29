package x.scratch.blockchain

import java.time.Duration
import java.time.Instant
import java.time.Instant.EPOCH

fun <R> timing(block: () -> R): Pair<R, Duration> {
    val start = Instant.now()
    val result = block()!!
    val end = Instant.now()
    return result to Duration.between(start, end)
}

fun main() {
    var running = timing {
        Blockchain.new()
    }
    println("Genesis timing -> ${running.second}")
    var blockchain = running.first
    blockchain.checkAndDump()

    running = timing {
        blockchain.newBlock("Hello, world!")
    }
    println("New block timing -> ${running.second}")
    blockchain.checkAndDump()

    println()

    running = timing {
        Blockchain.new(
            difficulty = 4,
            timestamp = EPOCH
        )
    }
    println("Genesis timing -> ${running.second}")
    blockchain = running.first
    blockchain.checkAndDump()

    running = timing {
        blockchain.newBlock(
            data = mapOf("greeting" to "Hello, world!"),
            timestamp = blockchain.first().timestamp.plusMillis(1L)
        )
    }
    println("New block timing -> ${running.second}")
    blockchain.checkAndDump()
}

private fun Blockchain.checkAndDump() {
    check()

    println("blockchain -> $this")
    println("difficulty -> $difficulty")
    println("latest -> ${last()}")
    println("first genesis -> ${first().genesis}")
    println("last genesis -> ${last().genesis}")
    println("first by index -> ${this[this[0].hash]}")
    println("last by hash -> ${this[last().hash]}")

    for (block in this) println("block#${block.index} -> $block")
}
