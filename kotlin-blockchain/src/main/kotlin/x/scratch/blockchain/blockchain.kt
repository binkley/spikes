package x.scratch.blockchain

import java.security.MessageDigest
import java.time.Instant
import java.time.Instant.EPOCH
import java.util.Objects
import kotlin.Int.Companion.MAX_VALUE

fun main() {
    var blockchain = Blockchain.new(
        difficulty = "00"
    )
    blockchain.dump()

    blockchain.add("Hello, world!")
    blockchain.dump()

    // Testing example
    blockchain = Blockchain.new(
        difficulty = "",
        timestamp = EPOCH
    )
    blockchain.dump()

    blockchain.add(
        data = mapOf("greeting" to "Hello, world!"),
        timestamp = Instant.ofEpochMilli(1L)
    )
    blockchain.dump()
}

private fun Blockchain.dump() {
    println(this)
    println("current=${last()}")
    println("genesis=${first().genesis}")
    println("genesis=${last().genesis}")
    println("first=${this[this[0].hash]}")
    println("first=${this[last().hash]}")

    for (block in this) println(block)
}

class Blockchain private constructor(
    difficulty: String,
    timestamp: Instant,
    private val _chain: MutableList<Block> = mutableListOf(
        Block.first(
            difficulty,
            timestamp
        )
    )
) : List<Block> by _chain {
    fun add(data: Any, timestamp: Instant = Instant.now()) {
        _chain += last().next(data, timestamp)
    }

    operator fun get(hash: String) = _chain.firstOrNull { hash == it.hash }

    override fun equals(other: Any?): Boolean {
        return this === other
                || other is Blockchain
                && _chain == other._chain
    }

    override fun hashCode() = Objects.hash(_chain)

    override fun toString() =
        "${super.toString()}{chain=$_chain}"

    companion object {
        fun new(
            difficulty: String = "",
            timestamp: Instant = Instant.now()
        ) = Blockchain(difficulty, timestamp)
    }
}

private val sha256 = MessageDigest.getInstance("SHA-256")
private val genesisHash = "0".repeat(64)

class Block private constructor(
    val index: Long,
    val data: Any,
    val previousHash: String,
    val difficulty: String,
    val timestamp: Instant
) {
    val hash: String = hashWithProofOfWork()

    val genesis: Boolean
        get() = 0L == index

    fun next(data: Any, timestamp: Instant = Instant.now()) =
        Block(index + 1, data, hash, difficulty, timestamp)

    private fun hashWithProofOfWork(): String {
        fun hashWithNonce(nonce: Int) = sha256
            .digest("$nonce$index$timestamp$difficulty$previousHash$data".toByteArray())
            .joinToString("") { "%02x".format(it) }

        for (nonce in 0..MAX_VALUE) {
            val hash = hashWithNonce(nonce)
            if (hash.startsWith(difficulty)) return hash
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
        "${super.toString()}{index=$index, timestamp=$timestamp, data=$data, hash=$hash, previousHash=$previousHash}"

    companion object {
        fun first(
            difficulty: String,
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
