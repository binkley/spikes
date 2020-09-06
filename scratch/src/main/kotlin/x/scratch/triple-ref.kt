package x.scratch

/**
 * See https://youtu.be/1s0w_p5HEuY
 */
fun main() {
    println("==TRIPLE REF")

    println()
    println("----")
    var first = Ptr("banana")
    println(first)
    var head = Ptr(first)
    println(head)
    println("----")
    insert(head, Ptr("apple"))
    println(head.deref)
    println("----")
    insert(head, Ptr("carrot"))
    println(head.deref)
}

private class Ptr<T : Comparable<T>>(
    var deref: T,
) : Comparable<Ptr<T>> {
    var next: Ptr<T>? = null

    override fun compareTo(other: Ptr<T>) = deref.compareTo(other.deref)

    override fun toString(): String {
        val pretty = mutableListOf<T>()
        var here: Ptr<T>? = this
        while (null != here) {
            pretty += here.deref
            here = here.next
        }
        return pretty.toString()
    }
}

private fun <T : Comparable<T>> insert(head: Ptr<Ptr<T>>, item: Ptr<T>) {
    var p = head
    while (null != p.deref) {
        if (item.deref <= p.deref.deref) break
        p = Ptr(p.deref.next as Ptr<T>)
    }
    item.next = p.deref
    p.deref = item
}
