package x.scratch.blockchain

import java.security.MessageDigest

internal fun hashPrefixForDifficulty(difficulty: Int) =
    "0".repeat(difficulty)

internal fun hashForBlock(
    digest: MessageDigest,
    toHashForBlock: String
) = digest
    .digest(toHashForBlock.toByteArray())
    .joinToString("") { "%02x".format(it) }

internal fun Map<String, TimedHash>.equivalentTo(other: Map<String, TimedHash>)
        : Boolean {
    return map {
        it.key to it.value.hash
    } == other.map {
        it.key to it.value.hash
    }
}
