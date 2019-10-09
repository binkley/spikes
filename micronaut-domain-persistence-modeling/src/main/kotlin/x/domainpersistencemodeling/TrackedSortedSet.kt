package x.domainpersistencemodeling

import java.util.*

internal class TrackedSortedSet<T : Comparable<T>>(
        initial: Collection<T>,
        private val added: (T, Set<T>) -> Unit,
        private val removed: (T, Set<T>) -> Unit)
    : AbstractMutableSet<T>() {
    private val buf = TreeSet(initial)

    override val size: Int
        get() = buf.size

    override fun add(element: T): Boolean {
        val add = buf.add(element)
        added(element, buf)
        return add
    }

    override fun iterator(): MutableIterator<T> {
        return object : MutableIterator<T> {
            private val it = buf.iterator()
            private var curr: T? = null

            override fun hasNext() = it.hasNext()

            override fun next(): T {
                val next = it.next()
                curr = next
                return next
            }

            override fun remove() {
                it.remove()
                removed(curr!!, buf)
            }
        }
    }
}
