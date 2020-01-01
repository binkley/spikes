package x.scratch.blockchain

import x.scratch.blockchain.Blockchain.Block
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Objects

private const val genesisData = "Genesis"
private val genesisHash = TimedHash("0", Duration.ZERO)

data class TimedHash(val hash: String, val timing: Duration)

class Blockchain private constructor(
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
        "${super.toString()}{difficulty=$difficulty, chain=$chain}"

    fun pretty(): String {
        val buf = StringBuilder()

        buf.append("BLOCKCHAIN difficulty: ")
        buf.append(difficulty)

        for (block in chain) {
            buf.append("\n---\n")
            block.pretty(buf)
        }

        return buf.toString()
    }

    /**
     * Validates the blockchain.  This is an expensive operation.  Please use
     * in simple tests only.
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

            if (block.previousHashes.map {
                    it.key to it.value.hash
                } == previousHashes.map {
                    it.key to it.value.hash
                })
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
        val previousHashes: Map<String, TimedHash>,
        private var _nonce: Int = Int.MIN_VALUE
    ) {
        val hashes = allHashesWithProofOfWork(functions)

        val nonce: Int
            get() = _nonce

        /**
         * Validates the block.  This is an expensive operation.  Please use
         * in simple tests only.
         */
        fun verify() {
            // TODO: This is horrid.  It runs as slowly as creating the block
            if (hashes.map {
                    it.key to it.value.hash
                } != allHashesWithProofOfWork(hashes.keys).map {
                    it.key to it.value.hash
                })
                error("Corrupted: $this")

            val hashPrefix = computeHashPrefixFromDifficulty(difficulty)
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

        private fun allHashesWithProofOfWork(functions: Set<String>)
                : Map<String, TimedHash> {
            val hashPrefix = computeHashPrefixFromDifficulty(difficulty)
            val digests = functions.map { function ->
                // TODO: Kotlin, how to say non-null if the JDK throws?
                function to MessageDigest.getInstance(function)!!
            }.toMap()  // Memoize

            fun oneHashWithProofOfWork(function: String): TimedHash {
                // TODO: Use genesis hash, or recompute to start of chain?
                val previousHash =
                    previousHashes.getOrDefault(function, genesisHash).hash
                val start = Instant.now()
                for (nonce in 0..Int.MAX_VALUE) {
                    val hash = hashForBlock(
                        // TODO: Kotlin, how to say map cannot be missing keys?
                        digests[function]!!,
                        "$nonce$height$timestamp$data$purpose$previousHash"
                    )

                    if (hash.startsWith(hashPrefix)) {
                        val timing = Duration.between(Instant.now(), start)
                        this._nonce = nonce
                        return TimedHash(hash, timing)
                    }
                }

                error("Unable to complete work: $this")
            }

            return functions.map { function ->
                function to oneHashWithProofOfWork(function)
            }.toMap()
        }

        fun pretty(buf: StringBuilder) {
            buf.append("BLOCK #")
            buf.append(height)
            buf.append(" @ ")
            buf.append(timestamp)
            buf.append("\n* Purpose: ")
            buf.append(purpose)
            buf.append("\n* Hashes:")
            hashes.forEach { (function, timedHash) ->
                buf.append("\n    - ")
                buf.append(function)
                buf.append(" (")
                buf.append(timedHash.timing)
                buf.append("): ")
                buf.append(timedHash.hash)
            }
            buf.append("\n* Previous hashes:")
            previousHashes.forEach { (function, timedHash) ->
                buf.append("\n    - ")
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

        override fun equals(other: Any?) = this === other
                || other is Block
                && hashes == other.hashes

        override fun hashCode() = Objects.hash(hashes)

        override fun toString() =
            "${super.toString()}{height=$height, timestamp=$timestamp, data=$data, purpose=$purpose, hashes=$hashes, previousHashes=$previousHashes, nonce=$nonce}"
    }

    companion object {
        val DEFAULT_FUNCTIONS = setOf("SHA-256") // SHA2

        fun new(
            purpose: String,
            difficulty: Int = 0,
            initialFunctions: Set<String> = DEFAULT_FUNCTIONS,
            genesisTimestamp: Instant = Instant.now()
        ) = Blockchain(
            purpose = purpose,
            difficulty = difficulty,
            initialFunctions = initialFunctions,
            genesisTimestamp = genesisTimestamp
        )
    }
}
