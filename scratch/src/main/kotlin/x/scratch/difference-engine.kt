package x.scratch

fun main() {
    println("==DIFFERENCE ENGINE")

    val ours = listOf(
        Left("ALICE", 2),  // common, unchanged
        Left("BOB", 3),  // added
        Left("DAVE", 5), // common, changed
    )
    val theirs = listOf(
        Right("ALICE", "2"),  // common, unchanged
        Right("CAROL", "4"),  // removed
        Right("DAVE", "6"), // common, changed
    )

    val engine = DifferenceEngine(Left::key, Right::key) { a, b ->
        a.key == b.key && "${a.satelliteData}" == b.satelliteData
    }
    val diff = engine.diff(ours, theirs)

    println()
    println("ADDED -> ${diff.addedByUs}")
    println("REMOVED -> ${diff.removedByUs}")
    println("CHANGED -> ${diff.changedByUs}")
}

class DifferenceEngine<KEY, OURS, THEIRS>(
    private val ourCommonKey: (OURS) -> KEY,
    private val theirCommonKey: (THEIRS) -> KEY,
    private val equivalent: (OURS, THEIRS) -> Boolean,
) {
    fun diff(
        ours: Collection<OURS>,
        theirs: Collection<THEIRS>,
    ): Difference<KEY, OURS, THEIRS> {
        return Difference(
            ourCommonKey,
            theirCommonKey,
            equivalent,
            ours,
            theirs
        )
    }
}

class Difference<KEY, OURS, THEIRS>(
    ourCommonKey: (OURS) -> KEY,
    theirCommonKey: (THEIRS) -> KEY,
    equivalent: (OURS, THEIRS) -> Boolean,
    ours: Collection<OURS>,
    theirs: Collection<THEIRS>,
) {
    val addedByUs: List<OURS>
    val removedByUs: List<THEIRS>
    val changedByUs: List<Pair<OURS, THEIRS>>

    init {
        val oursByKey = ours.map {
            ourCommonKey(it) to it
        }.toMap()
        val theirsByKey = theirs.map {
            theirCommonKey(it) to it
        }.toMap()

        addedByUs = oursByKey.entries
            .filter { !theirsByKey.containsKey(it.key) }
            .map { it.value }
            .toList()
        removedByUs = theirsByKey.entries
            .filter { !oursByKey.containsKey(it.key) }
            .map { it.value }
            .toList()
        // Suppress is unfortunate: Kotlin compiler does not understand after
        // the filter line that "second" cannot be NULL.  This the rare case
        // where Java code compiles more efficiently
        @Suppress("UNCHECKED_CAST")
        changedByUs = oursByKey.entries
            .map { it.value to theirsByKey[it.key] }
            .filter {
                val them = it.second
                null != them && !equivalent(it.first, them)
            }
            .toList() as List<Pair<OURS, THEIRS>>
    }
}

private data class Left(val key: String, val satelliteData: Int)

private data class Right(val key: String, val satelliteData: String)
