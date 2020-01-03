package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Objects

private val genesisHash = TimedHash("0", Duration.ZERO, 0)

private val digests = mutableMapOf<String, MessageDigest>()
private fun digest(function: String) =
    digests.computeIfAbsent(function) {
        MessageDigest.getInstance(it)!!
    }

data class TimedHash(val hash: String, val timing: Duration, val nonce: Int)

class Blockchain private constructor(
    val genesisData: String,
    purpose: String,
    val difficulty: Int, // TODO: Property of the chain, not the block
    initialFunctions: Set<String>,
    genesisTimestamp: Instant,
    // TODO: Kotlin, how to delegate to a non-ctor arg?
    private val chain: MutableList<Block> = mutableListOf()
) : List<Block> by chain {
    init {
        chain += genesisBlock(
            purpose = purpose,
            initialFunctions = initialFunctions,
            genesisTimestamp = genesisTimestamp
        )
    }

    fun newBlock(
        data: Any,
        purpose: String,
        functions: Set<String> = previousFunctions(),
        timestamp: Instant = Instant.now()
    ) = apply {
        chain += last().next(
            data = data,
            purpose = purpose,
            functions = functions,
            timestamp = timestamp
        )
    }

    /**
     * Looks up a block by its digest function and hash, and returns `null`
     * if none found.  Example:
     * ```
     * val aBlock = blockchain["SHA-256", "d7cce22abb7945c814718fb71c5a2d27f2da47a39a01aee14e5b2a1cddb9bdd9"]
     * ```
     * Use the `List` index operator to lookup a block by height.
     */
    operator fun get(function: String, hash: String) =
        firstOrNull { hash == it.hashes[function]?.hash }

    override fun equals(other: Any?) = this === other
            || other is Blockchain
            && chain == other.chain

    override fun hashCode() = Objects.hash(chain)

    override fun toString() =
        "${super.toString()}{genesisData=$genesisData, difficulty=$difficulty, chain=$chain}"

    /**
     * Validates the blockchain.
     *
     * @todo Do not require an input
     */
    fun verify(functions: Set<String>) {
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

            if (block.previousHashes.equivalentTo(previousHashes))
                previousHashes = block.hashes
            else error("Corrupted: $chain")

            block.verify()
        }
    }

    private fun genesisBlock(
        purpose: String,
        initialFunctions: Set<String>,
        genesisTimestamp: Instant
    ) = Block(
        height = 0,
        timestamp = genesisTimestamp,
        data = genesisData,
        purpose = purpose,
        functions = initialFunctions,
        previousHashes = initialFunctions.map {
            it to genesisHash
        }.toMap()
    )

    private fun previousFunctions() = last().hashes.keys

    inner class Block internal constructor(
        val height: Long,
        val timestamp: Instant,
        val data: Any,
        val purpose: String,
        functions: Set<String>,
        val previousHashes: Map<String, TimedHash>
    ) {
        val hashes = allHashesWithProofOfWork(functions)

        /** Validates the block. */
        fun verify() {
            val originalHashes = hashes.map { (function, timedHash) ->
                function to timedHash.hash
            }.toMap()
            val recomputedHashes = hashes.map { (function, timedHash) ->
                function to hashWithNonce(function, timedHash.nonce)
            }.toMap()

            if (originalHashes != recomputedHashes)
                error("Corrupted: $this")

            val hashPrefix = hashPrefixForDifficulty(difficulty)
            for (timedHash in hashes.values)
                if (!timedHash.hash.startsWith(hashPrefix))
                    error("Too easy: $this")
        }

        fun next(
            data: Any,
            purpose: String,
            functions: Set<String>,
            timestamp: Instant
        ) = Block(
            height = height + 1,
            timestamp = timestamp,
            data = data,
            purpose = purpose,
            functions = functions,
            previousHashes = hashes
        )

        private fun hashWithNonce(
            function: String,
            nonce: Int
        ): String {
            // TODO: Use genesis hash, or something cleverer?
            val previousHash =
                previousHashes.getOrDefault(function, genesisHash).hash
            return hashForBlock(
                digest(function),
                "$nonce$height$timestamp$data$purpose$previousHash"
            )
        }

        private fun allHashesWithProofOfWork(functions: Set<String>)
                : Map<String, TimedHash> {
            val hashPrefix = hashPrefixForDifficulty(difficulty)

            fun oneHashWithProofOfWork(function: String): TimedHash {
                val start = Instant.now()

                for (nonce in 0..Int.MAX_VALUE) {
                    val hash = hashWithNonce(function, nonce)

                    if (hash.startsWith(hashPrefix)) {
                        val timing = Duration.between(Instant.now(), start)
                        return TimedHash(hash, timing, nonce)
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
            "${super.toString()}{height=$height, timestamp=$timestamp, data=$data, purpose=$purpose, hashes=$hashes, previousHashes=$previousHashes}"
    }

    companion object {
        val DEFAULT_FUNCTIONS = setOf("SHA-256") // SHA2

        fun new(
            genesisData: String,
            purpose: String,
            difficulty: Int = 0,
            initialFunctions: Set<String> = DEFAULT_FUNCTIONS,
            genesisTimestamp: Instant = Instant.now()
        ) = Blockchain(
            genesisData = genesisData,
            purpose = purpose,
            difficulty = difficulty,
            initialFunctions = initialFunctions,
            genesisTimestamp = genesisTimestamp
        )
    }
}
