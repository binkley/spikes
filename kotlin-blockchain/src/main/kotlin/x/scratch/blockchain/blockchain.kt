package x.scratch.blockchain

import java.security.MessageDigest
import java.time.Instant
import java.time.Instant.EPOCH
import java.util.Objects
import kotlin.Int.Companion.MAX_VALUE

fun main() {
    var blockchain = Blockchain.new(
        difficulty = 2
    )
    blockchain.dump()

    blockchain.add("Hello, world!")
    blockchain.dump()
    blockchain.verify()

    println()

    // Testing example
    blockchain = Blockchain.new(
        timestamp = EPOCH
    )
    blockchain.dump()

    blockchain.add(
        data = mapOf("greeting" to "Hello, world!"),
        timestamp = blockchain.first().timestamp.plusMillis(1L)
    )
    blockchain.dump()
    blockchain.verify()
}

private fun Blockchain.dump() {
    println("blockchain -> $this")
    println("difficulty -> $difficulty")
    println("latest -> ${last()}")
    println("first genesis -> ${first().genesis}")
    println("last genesis -> ${last().genesis}")
    println("first by index -> =${this[this[0].hash]}")
    println("last by hash -> ${this[last().hash]}")

    for (block in this) println(block)
}

class Blockchain private constructor(
    val difficulty: Int,
    private val chain: MutableList<Block>
) : List<Block> by chain {
    init {
        verify()
    }

    fun add(data: Any, timestamp: Instant = Instant.now()) {
        chain += last().next(data, timestamp)
    }

    operator fun get(hash: String) = firstOrNull { hash == it.hash }

    override fun equals(other: Any?) = this === other
            || other is Blockchain
            && chain == other.chain

    override fun hashCode() = Objects.hash(chain)

    override fun toString() = "${super.toString()}{chain=$chain}"

    fun verify() {
        var previousIndex = -1L
        var previousTimestamp = Instant.MIN
        var previousHash = genesisHash
        val hashPrefix = "0".repeat(difficulty)

        for (block in chain) {
            if (block.index > previousIndex)
                previousIndex = block.index
            else throw IllegalStateException("Out of sequence: $chain")

            if (block.timestamp.isAfter(previousTimestamp))
                previousTimestamp = block.timestamp
            else throw IllegalStateException("Out of order: $chain")

            if (block.previousHash == previousHash)
                previousHash = block.hash
            else throw IllegalStateException("Corrupted: $chain")

            if (!block.hash.startsWith(hashPrefix))
                throw IllegalStateException("Too easy: $chain")
        }
    }

    companion object {
        fun new(
            difficulty: Int = 0,
            timestamp: Instant = Instant.now()
        ) = Blockchain(
            difficulty,
            mutableListOf(
                Block.first(
                    difficulty,
                    timestamp
                )
            )
        )
    }
}

private val sha256 = MessageDigest.getInstance("SHA-256")
private val genesisHash = "0".repeat(64)

class Block private constructor(
    val index: Long,
    val timestamp: Instant,
    val data: Any,
    val previousHash: String,
    val difficulty: Int
) {
    val hash: String = hashWithProofOfWork()

    val genesis: Boolean
        get() = 0L == index

    fun next(data: Any, timestamp: Instant = Instant.now()) =
        Block(
            index = index + 1,
            timestamp = timestamp,
            data = data,
            previousHash = hash,
            difficulty = difficulty
        )

    private fun hashWithProofOfWork(): String {
        val hashPrefix = "0".repeat(difficulty)
        fun hashWithNonce(nonce: Int) = sha256
            .digest("$nonce$index$timestamp$hashPrefix$previousHash$data".toByteArray())
            .joinToString("") { "%02x".format(it) }

        for (nonce in 0..MAX_VALUE) {
            val hash = hashWithNonce(nonce)
            if (hash.startsWith(hashPrefix)) return hash
        }

        throw IllegalStateException("Unable to complete work: $this")
    }

    override fun equals(other: Any?): Boolean {
        return this === other
                || other is Block
                && hash == other.hash
    }

    override fun hashCode() = Objects.hash(hash)

    override fun toString() =
        "${super.toString()}{index=$index, timestamp=$timestamp, data=$data, hash=$hash, previousHash=$previousHash, difficulty=$difficulty}"

    companion object {
        fun first(
            difficulty: Int,
            timestamp: Instant = Instant.now()
        ) = Block(
            index = 0,
            data = "Genesis",
            previousHash = genesisHash,
            difficulty = difficulty,
            timestamp = timestamp
        )
    }
}
