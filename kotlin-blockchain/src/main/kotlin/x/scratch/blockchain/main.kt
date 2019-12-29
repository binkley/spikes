package x.scratch.blockchain

import java.time.Instant.EPOCH

fun main() {
    var blockchain = Blockchain.new(
        difficulty = 2
    )
    blockchain.dump()

    blockchain.newBlock("Hello, world!")
    blockchain.check()
    blockchain.dump()

    println()

    // Testing example
    blockchain = Blockchain.new(
        timestamp = EPOCH
    )
    blockchain.dump()

    blockchain.newBlock(
        data = mapOf("greeting" to "Hello, world!"),
        timestamp = blockchain.first().timestamp.plusMillis(1L)
    )
    blockchain.check()
    blockchain.dump()
}

private fun Blockchain.dump() {
    println("blockchain -> $this")
    println("difficulty -> $difficulty")
    println("latest -> ${last()}")
    println("first genesis -> ${first().genesis}")
    println("last genesis -> ${last().genesis}")
    println("first by index -> ${this[this[0].hash]}")
    println("last by hash -> ${this[last().hash]}")

    for (block in this) println("block#${block.index} -> $block")
}
