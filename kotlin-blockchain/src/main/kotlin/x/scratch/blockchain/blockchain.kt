package x.scratch.blockchain

import java.security.MessageDigest
import java.time.Instant
import java.util.Objects
import kotlin.Int.Companion.MAX_VALUE

fun main() {
    val firstBlock = Block.first("00")
    println("$firstBlock")
    val nextBlock = firstBlock.next("Hello, world!")
    println("$nextBlock")
}

private val sha256 = MessageDigest.getInstance("SHA-256")

fun Block.Companion.first(difficulty: String = "") =
    Block(
        "Genesis",
        "0".repeat(64),
        difficulty
    )

class Block(
    val data: String,
    val previousHash: String,
    val difficulty: String,
    val timestamp: Instant = Instant.now()
) {
    val hash: String = hashWithProofOfWork()

    fun next(data: String) = Block(data, hash, difficulty)

    private fun hashWithProofOfWork(): String {
        fun hashWithNonce(nonce: Int) = sha256
            .digest("$nonce$timestamp$previousHash$data".toByteArray())
            .joinToString("") { "%02x".format(it) }

        for (nonce in 0..MAX_VALUE) {
            val hash = hashWithNonce(nonce)
            if (hash.startsWith(difficulty)) return hash
        }

        throw IllegalStateException("Unable to hash with difficulty: $this")
    }

    override fun equals(other: Any?): Boolean {
        return this === other
                || other is Block
                && hash == other.hash
    }

    override fun hashCode() = Objects.hash(hash)

    override fun toString() =
        "${super.toString()}{timestamp=$timestamp, data=$data, hash=$hash, previousHash=$previousHash}"

    companion object
}
