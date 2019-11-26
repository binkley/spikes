package x.domainpersistencemodeling

import java.util.TreeSet

internal class TrackedSortedSet<T : Comparable<T>>(
    initial: Collection<T>,
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
}
