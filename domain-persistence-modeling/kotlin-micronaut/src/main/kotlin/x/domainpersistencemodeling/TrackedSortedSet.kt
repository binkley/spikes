package x.domainpersistencemodeling

import java.util.TreeSet

internal class TrackedSortedSet<T : Comparable<T>>(
    private var initial: Set<T>,
    private val addOne: (T, MutableSet<T>) -> Unit,
    private val removeOne: (T, MutableSet<T>) -> Unit
) : AbstractMutableSet<T>() {
    private val current: MutableSet<T> = TreeSet(initial)

    override val size: Int
        get() = current.size

    override fun add(element: T): Boolean {
        if (!current.add(element))
            throw DomainException("Already present: $element")
        addOne(element, current)
        return true
    }

    override fun remove(element: T): Boolean {
        if (!super.remove(element))
            throw DomainException("Not present: $element")
        removeOne(element, current)
        return true
    }

    override fun iterator() = current.iterator()

    fun reset() {
        initial = TreeSet(current)
    }

    fun added() = added { true }
    fun added(mutated: (T) -> Boolean): Set<T> {
        val added = TreeSet(current)
        added.removeAll(initial)
        return added.filter(mutated).toSortedSet()
    }

    fun removed() = removed { true }
    fun removed(mutated: (T) -> Boolean): Set<T> {
        val removed = TreeSet(initial)
        removed.removeAll(current)
        return removed.filter(mutated).toSortedSet()
    }

    fun changed(mutated: (T) -> Boolean): Set<T> {
        val changed = TreeSet(initial)
        changed.retainAll(current)
        return changed.filter(mutated).toSortedSet()
    }
}
