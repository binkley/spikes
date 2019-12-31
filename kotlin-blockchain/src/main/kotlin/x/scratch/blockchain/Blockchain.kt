package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.security.MessageDigest
import java.time.Instant
import java.util.Objects

private val genesisHashes = mapOf("SHA-256" to "0")

class Blockchain private constructor(
    val difficulty: Int,
    firstFunctions: Set<String>,
    firstTimestamp: Instant,
    // TODO: To delegate List to chain, need a chain in ctor, not a property
    private val chain: MutableList<Block> = mutableListOf()
) : List<Block> by chain {
    init {
        chain += firstBlock(
            functions = firstFunctions,
            timestamp = firstTimestamp
        )
    }

    fun newBlock(data: Any, timestamp: Instant = Instant.now()): Blockchain {
        chain += last().let {
            it.next(
                data = data,
                functions = it.hashes.keys,
                timestamp = timestamp
            )
        }
        return this
    }

    operator fun get(function: String, hash: String) =
        firstOrNull { hash == it.hashes[function] }

    override fun equals(other: Any?) = this === other
            || other is Blockchain
            && chain == other.chain

    override fun hashCode() = Objects.hash(chain)

    override fun toString() =
        "${super.toString()}{difficulty=$difficulty, chain=$chain}"

    fun check() {
        var previousIndex = -1L
        var previousTimestamp = Instant.MIN
        var previousHashes = genesisHashes
        val hashPrefix = "0".repeat(difficulty)

        for (block in chain) {
            if (block.index == previousIndex + 1)
                previousIndex = block.index
            else throw IllegalStateException("Out of sequence: $chain")

            // TODO: Is it legit to have same timestamp for blocks?
            if (block.timestamp.isAfter(previousTimestamp))
                previousTimestamp = block.timestamp
            else throw IllegalStateException("Out of order: $chain")

            if (block.previousHashes == previousHashes)
                previousHashes = block.hashes
            else throw IllegalStateException("Corrupted: $chain")

            for (hash in block.hashes.values)
                if (!hash.startsWith(hashPrefix))
                    throw IllegalStateException("Too easy: $chain")

            block.check()
        }
    }

    private fun firstBlock(
        functions: Set<String>,
        timestamp: Instant
    ) =
        Block(
            index = 0,
            timestamp = timestamp,
            data = "Genesis",
            functions = functions,
            previousHashes = genesisHashes
        )

    inner class Block internal constructor(
        val index: Long,
        val timestamp: Instant,
        val data: Any,
        functions: Set<String>,
        val previousHashes: Map<String, String>,
        var nonce: Int = Int.MIN_VALUE
    ) {
        val hashes: Map<String, String> = hashesWithProofOfWork(functions)

        val genesis: Boolean
            get() = 0L == index

        fun next(
            data: Any,
            functions: Set<String>,
            timestamp: Instant
        ) =
            Block(
                index = index + 1,
                timestamp = timestamp,
                data = data,
                functions = functions,
                previousHashes = hashes
            )

        fun check() {
            if (hashes != hashesWithProofOfWork(hashes.keys))
                throw IllegalStateException("Corrupted: $this")
        }

        private fun hashesWithProofOfWork(functions: Set<String>)
                : Map<String, String> {
            val hashPrefix = "0".repeat(difficulty)

            fun hashWithNonce(function: String, nonce: Int): String {
                val previousHash = previousHashes.getOrDefault(function, "0")
                return MessageDigest
                    .getInstance(function)
                    .digest("$nonce$index$timestamp$hashPrefix$previousHash$data".toByteArray())
                    .joinToString("") { "%02x".format(it) }
            }

            fun hash(function: String): String {
                for (nonce in 0..Int.MAX_VALUE) {
                    val hash = hashWithNonce(function, nonce)
                    if (hash.startsWith(hashPrefix)) {
                        this.nonce = nonce
                        return hash
                    }
                }

                throw IllegalStateException("Unable to complete work: $this")
            }

            return functions.map {
                it to hash(it)
            }.toMap()
        }

        override fun equals(other: Any?): Boolean {
            return this === other
                    || other is Block
                    && hashes == other.hashes
        }

        override fun hashCode() =
            Objects.hash(hashes)

        override fun toString() =
            "${super.toString()}{index=$index, timestamp=$timestamp, data=$data, hashes=$hashes, previousHash=$previousHashes, nonce=$nonce}"
    }

    companion object {
        fun new(
            difficulty: Int = 0,
            functions: Set<String> = setOf("SHA-256"),
            timestamp: Instant = Instant.now()
        ) = Blockchain(
            difficulty = difficulty,
            firstFunctions = functions,
            firstTimestamp = timestamp
        )
    }
}
