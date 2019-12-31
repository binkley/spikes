package x.scratch.blockchain

import java.security.MessageDigest

internal fun computeHashPrefixFromDifficulty(difficulty: Int) =
    "0".repeat(difficulty)

internal fun hashForBlock(
    digest: MessageDigest,
    toHashForBlock: String
) = digest
    .digest(toHashForBlock.toByteArray())
    .joinToString("") { "%02x".format(it) }
