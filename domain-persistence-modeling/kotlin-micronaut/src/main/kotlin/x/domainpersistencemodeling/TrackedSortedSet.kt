package x.domainpersistencemodeling

import java.util.*
import kotlin.reflect.KProperty

// TODO: Teach arity, so this can check for bugs
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

    override fun iterator() = object : MutableIterator<T> {
        private val it = current.iterator()
        private var last: T? = null

        override fun hasNext() = it.hasNext()

        override fun next(): T {
            val next = it.next()
            last = next
            return next
        }

        override fun remove() {
            it.remove()
            removeOne(last!!, current)
        }
    }

    fun reset() {
        initial = TreeSet(current)
    }

    fun added(mutator: (T) -> Boolean): Boolean {
        val added = TreeSet(current)
        added.removeAll(initial)
        return added.mutated(mutator)
    }

    fun removed(mutator: (T) -> Boolean): Boolean {
        val removed = TreeSet(initial)
        removed.removeAll(current)
        return removed.mutated(mutator)
    }

    fun changed(mutator: (T) -> Boolean): Boolean {
        val changed = TreeSet(initial)
        changed.retainAll(current)
        return changed.mutated(mutator)
    }

    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T? {
        return current.firstOrNull()
    }

    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T?
    ) {
        clear()
        value?.run { add(value) }
    }
}
