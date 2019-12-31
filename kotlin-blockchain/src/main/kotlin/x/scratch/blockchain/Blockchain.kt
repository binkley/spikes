package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.security.MessageDigest
import java.time.Instant
import java.util.Objects

private const val genesisData = "Genesis"
private const val genesisHash = "0"

class Blockchain private constructor(
    val difficulty: Int, // Property of the chain, not the block
    firstFunctions: Set<String>,
    firstTimestamp: Instant,
    // TODO: To delegate List to chain, must use in the ctor args
    private val chain: MutableList<Block> = mutableListOf()
) : List<Block> by chain {
    init {
        chain += genesisBlock(
            functions = firstFunctions,
            timestamp = firstTimestamp
        )
    }

    fun newBlock(
        data: Any,
        functions: Set<String> = last().hashes.keys,
        timestamp: Instant = Instant.now()
    ) = apply {
        chain += last().next(
            data = data,
            functions = functions,
            timestamp = timestamp
        )
    }

    operator fun get(function: String, hash: String) =
        firstOrNull { hash == it.hashes[function] }

    override fun equals(other: Any?) = this === other
            || other is Blockchain
            && chain == other.chain

    override fun hashCode() = Objects.hash(chain)

    override fun toString() =
        "${super.toString()}{difficulty=$difficulty, chain=$chain}"

    fun check(functions: Set<String>) {
        var previousHeight = -1L
        var previousTimestamp = Instant.MIN
        var previousHashes = functions.map {
            it to genesisHash
        }.toMap()

        for (block in chain) {
            if (block.height == previousHeight + 1)
                previousHeight = block.height
            else error("Out of sequence: $chain")

            // TODO: Is it legit to have same timestamp for blocks?
            if (block.timestamp.isAfter(previousTimestamp))
                previousTimestamp = block.timestamp
            else error("Out of order: $chain")

            if (block.previousHashes == previousHashes)
                previousHashes = block.hashes
            else error("Corrupted: $chain")

            block.check()
        }
    }

    private fun genesisBlock(
        functions: Set<String>,
        timestamp: Instant
    ) = Block(
        height = 0,
        timestamp = timestamp,
        data = genesisData,
        functions = functions,
        previousHashes = functions.map {
            it to genesisHash
        }.toMap()
    )

    inner class Block internal constructor(
        val height: Long,
        val timestamp: Instant,
        val data: Any,
        functions: Set<String>,
        val previousHashes: Map<String, String>,
        private var _nonce: Int = Int.MIN_VALUE
    ) {
        val hashes: Map<String, String> = allHashesWithProofOfWork(functions)
        val nonce: Int
            get() = _nonce

        val genesis: Boolean
            get() = 0L == height

        fun check() {
            if (hashes != allHashesWithProofOfWork(hashes.keys))
                error("Corrupted: $this")

            val hashPrefix = "0".repeat(difficulty)
            for (hash in hashes.values)
                if (!hash.startsWith(hashPrefix))
                    error("Too easy: $this")
        }

        fun next(
            data: Any,
            functions: Set<String>,
            timestamp: Instant
        ) = Block(
            height = height + 1,
            timestamp = timestamp,
            data = data,
            functions = functions,
            previousHashes = hashes
        )

        private fun allHashesWithProofOfWork(functions: Set<String>)
                : Map<String, String> {
            val hashPrefix = "0".repeat(difficulty)
            val digests = functions.map { function ->
                function to MessageDigest.getInstance(function)
            }.toMap()

            fun hashWithNonce(function: String, nonce: Int): String {
                // TODO: Use genesis hash, or recompute to start of chain?
                val previousHash =
                    previousHashes.getOrDefault(function, genesisHash)

                return (digests[function]
                    ?: error("BUG: No message digest for function: $function"))
                    .digest("$nonce$height$timestamp$hashPrefix$previousHash$data".toByteArray())
                    .joinToString("") { "%02x".format(it) }
            }

            fun oneHashWithProofOfWork(function: String): String {
                for (nonce in 0..Int.MAX_VALUE) {
                    val hash = hashWithNonce(function, nonce)
                    if (hash.startsWith(hashPrefix)) {
                        this._nonce = nonce
                        return hash
                    }
                }

                error("Unable to complete work: $this")
            }

            return functions.map { function ->
                function to oneHashWithProofOfWork(function)
            }.toMap()
        }

        override fun equals(other: Any?) = this === other
                || other is Block
                && hashes == other.hashes

        override fun hashCode() = Objects.hash(hashes)

        override fun toString() =
            "${super.toString()}{height=$height, timestamp=$timestamp, data=$data, hashes=$hashes, previousHashes=$previousHashes, nonce=$nonce}"
    }

    companion object {
        val DEFAULT_FUNCTIONS = setOf("SHA-256")

        fun new(
            difficulty: Int = 0,
            functions: Set<String> = DEFAULT_FUNCTIONS,
            timestamp: Instant = Instant.now()
        ) = Blockchain(
            difficulty = difficulty,
            firstFunctions = functions,
            firstTimestamp = timestamp
        )
    }
}
