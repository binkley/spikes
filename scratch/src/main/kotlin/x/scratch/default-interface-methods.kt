package x.scratch

/**
 * See https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-m3-generating-default-methods-in-interfaces/
 */
fun main() {
    println("==DEFAULT INTERFACE METHODS")

    Nancy().sallyForth()
}

private interface BobHead {
    fun sallyForth() = println("Tallyho!")
}

private class Nancy : BobHead
