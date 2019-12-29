package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.security.MessageDigest
import java.time.Instant
import java.util.Objects

private const val genesisHash = "0"
private val sha2 = MessageDigest.getInstance("SHA-512")

class Blockchain private constructor(
    val difficulty: Int,
    firstTimestamp: Instant,
    // TODO: To delegate List to chain, need a chain in ctor, not a property
    private val chain: MutableList<Block> = mutableListOf()
) : List<Block> by chain {
    init {
        chain += firstBlock(firstTimestamp)
    }

    fun newBlock(data: Any, timestamp: Instant = Instant.now()): Blockchain {
        chain += last().next(data, timestamp)
        return this
    }

    operator fun get(hash: String) = firstOrNull { hash == it.hash }

    override fun equals(other: Any?) = this === other
            || other is Blockchain
            && chain == other.chain

    override fun hashCode() = Objects.hash(chain)

    override fun toString() =
        "${super.toString()}{difficulty=$difficulty, chain=$chain}"

    fun check() {
        var previousIndex = -1L
        var previousTimestamp = Instant.MIN
        var previousHash = genesisHash
        val hashPrefix = "0".repeat(difficulty)

        for (block in chain) {
            if (block.index == previousIndex + 1)
                previousIndex = block.index
            else throw IllegalStateException("Out of sequence: $chain")

            // TODO: Is it legit to have same timestamp for blocks?
            if (block.timestamp.isAfter(previousTimestamp))
                previousTimestamp = block.timestamp
            else throw IllegalStateException("Out of order: $chain")

            if (block.previousHash == previousHash)
                previousHash = block.hash
            else throw IllegalStateException("Corrupted: $chain")

            if (!block.hash.startsWith(hashPrefix))
                throw IllegalStateException("Too easy: $chain")

            block.check()
        }
    }

    private fun firstBlock(timestamp: Instant = Instant.now()) =
        Block(
            index = 0,
            timestamp = timestamp,
            data = "Genesis",
            previousHash = genesisHash
        )

    inner class Block internal constructor(
        val index: Long,
        val timestamp: Instant,
        val data: Any,
        val previousHash: String,
        var nonce: Int = Int.MIN_VALUE // TODO: A bad thing if it happens
    ) {
        val hash: String = hashWithProofOfWork()

        val genesis: Boolean
            get() = 0L == index

        fun next(data: Any, timestamp: Instant = Instant.now()) =
            Block(
                index = index + 1,
                timestamp = timestamp,
                data = data,
                previousHash = hash
            )

        fun check() {
            if (hash != hashWithProofOfWork())
                throw IllegalStateException("Corrupted: $this")
        }

        private fun hashWithProofOfWork(): String {
            val hashPrefix = "0".repeat(difficulty)
            fun hashWithNonce(nonce: Int) = sha2
                .digest("$nonce$index$timestamp$hashPrefix$previousHash$data".toByteArray())
                .joinToString("") { "%02x".format(it) }

            for (nonce in 0..Int.MAX_VALUE) {
                val hash = hashWithNonce(nonce)
                if (hash.startsWith(hashPrefix)) {
                    this.nonce = nonce
                    return hash
                }
            }

            throw IllegalStateException("Unable to complete work: $this")
        }

        override fun equals(other: Any?): Boolean {
            return this === other
                    || other is Block
                    && hash == other.hash
        }

        override fun hashCode() =
            Objects.hash(hash)

        override fun toString() =
            "${super.toString()}{index=$index, timestamp=$timestamp, data=$data, hash=$hash, previousHash=$previousHash, nonce=$nonce}"
    }

    companion object {
        fun new(
            difficulty: Int = 0,
            timestamp: Instant = Instant.now()
        ) = Blockchain(
            difficulty = difficulty,
            firstTimestamp = timestamp
        )
    }
}
