package x.domainpersistencemodeling

import java.util.*

internal class SaveBack<T>(
        private val buf: SortedSet<T>,
        private val added: (T, SortedSet<T>) -> Unit,
        private val removed: (T, SortedSet<T>) -> Unit)
    : AbstractMutableSet<T>() {
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
