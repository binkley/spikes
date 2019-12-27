package x.scratch.blockchain

import java.lang.System.currentTimeMillis
import java.security.MessageDigest
import java.util.Objects

@ExperimentalStdlibApi
fun main() {
    val block = Block.first()
    println("$block")
    println("${block.next("Hello, world!")}")
}

@ExperimentalStdlibApi
class Block(
    val data: String,
    val previousHash: String,
    val date: Long = currentTimeMillis()
) {
    val hash: String =
        MessageDigest.getInstance("SHA-256")
            // TODO: Formatted date
            .digest((date.toString() + previousHash + data).encodeToByteArray())
            .joinToString("") { "%02x".format(it) }

    fun next(data: String) = Block(data, hash)

    override fun equals(other: Any?): Boolean {
        return this === other
                || other is Block
                && hash == other.hash
    }

    override fun hashCode() = Objects.hash(hash)

    override fun toString() =
        "${super.toString()}{data=$data, previousHash=$previousHash, date=$date, hash=$hash}"

    companion object {
        fun first() = Block(
            "Genesis",
            "0000000000000000000000000000000000000000000000000000000000000000"
        )
    }
}
