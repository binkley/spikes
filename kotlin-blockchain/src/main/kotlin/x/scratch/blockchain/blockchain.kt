package x.scratch.blockchain

import java.security.MessageDigest
import java.time.Instant
import java.util.Objects
import kotlin.Int.Companion.MAX_VALUE

fun main() {
    val block = Block.first("00")
    println("$block")
    println("${block.next("Hello, world!")}")
}

private val sha256 = MessageDigest.getInstance("SHA-256")

class Block(
    val data: String,
    val previousHash: String,
    val difficulty: String,
    val timestamp: Instant = Instant.now()
) {
    val hash: String = hashWithProofOfWork()

    fun next(data: String) = Block(data, hash, difficulty)

    private fun hashWithProofOfWork(): String {
        fun hashWithNonce(nonce: Int) =
            sha256
                .digest((nonce.toString() + timestamp.toString() + previousHash + data).toByteArray())
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
        "${super.toString()}{data=$data, previousHash=$previousHash, timestamp=$timestamp, hash=$hash}"

    companion object {
        fun first(difficulty: String) = Block(
            "Genesis",
            "0000000000000000000000000000000000000000000000000000000000000000",
            difficulty
        )
    }
}
