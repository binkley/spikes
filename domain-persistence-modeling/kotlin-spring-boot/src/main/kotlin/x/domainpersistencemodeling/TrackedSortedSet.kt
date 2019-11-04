package x.domainpersistencemodeling

import java.util.TreeSet

internal class TrackedSortedSet<T : Comparable<T>>(
        initial: Collection<T>,
        private val added: (T, MutableSet<T>) -> Unit,
        private val removed: (T, MutableSet<T>) -> Unit)
    : AbstractMutableSet<T>() {
    private val sorted: MutableSet<T> = TreeSet(initial)

    override val size: Int
        get() = sorted.size

    override fun add(element: T): Boolean {
        if (!sorted.add(element))
            throw DomainException("Already present: $element")
        added(element, sorted)
        return true
    }

    override fun remove(element: T): Boolean {
        if (!super.remove(element))
            throw DomainException("Not present: $element")
        removed(element, sorted)
        return true
    }

    override fun iterator() = sorted.iterator()
}
