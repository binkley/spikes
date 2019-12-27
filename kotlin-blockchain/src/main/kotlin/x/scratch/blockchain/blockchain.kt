package x.scratch.blockchain

import java.lang.System.currentTimeMillis
import java.security.MessageDigest
import java.util.Objects

@ExperimentalStdlibApi
fun main() {
    val block = Block.first("00")
    println("$block")
    println("${block.next("Hello, world!")}")
}

@ExperimentalStdlibApi
class Block(
    val data: String,
    val previousHash: String,
    val difficulty: String,
    val timestamp: Long = currentTimeMillis()
) {
    val hash: String = hashWithProofOfWork()

    fun next(data: String) = Block(data, hash, difficulty)

    private fun hashWithProofOfWork(): String {
        var nonce = 0
        var hash = hashWithNonce(0)
        while (!hash.startsWith(difficulty)) {
            hash = hashWithNonce(++nonce)
        }
        return hash
    }

    private fun hashWithNonce(nonce: Int) =
        MessageDigest.getInstance("SHA-256")
            // TODO: Formatted date
            .digest((nonce.toString() + timestamp.toString() + previousHash + data).encodeToByteArray())
            .joinToString("") { "%02x".format(it) }

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
