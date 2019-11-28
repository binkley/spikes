package x.domainpersistencemodeling

import java.util.*

internal fun Set<String>.workAroundArrayTypeForPostgresWrite()
        : String =
    joinToString(",", "{", "}")

internal fun Set<String>.workAroundArrayTypeForPostgresRead()
        : MutableSet<String> {
    val raw = first().removeSurrounding("{", "}")
    return if (raw.isEmpty()) TreeSet()
    else TreeSet(raw.split(','))
}

internal fun <T> Set<T>.mutated(mutated: (T) -> Boolean) =
    map(mutated).fold(false) { a, b -> a || b }

internal fun Boolean.ifTrue(run: () -> Unit): Boolean {
    val isTrue = this
    if (isTrue) run()
    return isTrue
}

internal fun <Snapshot, Domain> TrackedSortedSet<Domain>.saveMutated()
        : Boolean
        where Domain : PersistableDomain<Snapshot, Domain>,
              Domain : Comparable<Domain> {
    var mutated = false

    added {
        it.save()
        true
    }.ifTrue { mutated = true }

    removed {
        it.save()
        true
    }.ifTrue { mutated = true }

    changed {
        it.changed.ifTrue {
            it.save()
        }
    }.ifTrue { mutated = true }

    if (mutated) reset()

    return mutated
}
