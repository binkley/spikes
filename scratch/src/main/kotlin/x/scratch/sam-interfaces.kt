package x.scratch

fun main() {
    println("==SAM INTERFACES")

    seuss("green eggs and ham") {
        println(it)
    }

    val sam = Sam { println(it.length) }
    println(sam.javaClass)
}

private fun interface Sam {
    fun iAm(message: String)
}

private fun seuss(message: String, sam: Sam) = sam.iAm(message)
