package x.domainpersistencemodeling

import java.util.TreeSet

internal class TrackedSortedSet<T : Comparable<T>>(
        initial: Collection<T>,
        private val added: (T, Set<T>) -> Unit,
        private val removed: (T, Set<T>) -> Unit)
    : AbstractMutableSet<T>() {
    private val sorted: MutableSet<T> = TreeSet(initial)

    override val size: Int
        get() = sorted.size

    override fun add(element: T): Boolean {
        val add = sorted.add(element)
        if (add) added(element, sorted)
        return add
    }

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {
            private val it = sorted.iterator()
            private var curr: T? = null

            override fun hasNext() = it.hasNext()

            override fun next(): T {
                curr = it.next()
                return curr!!
            }

            override fun remove() {
                it.remove()
                removed(curr!!, sorted)
            }
        }
    }
}
