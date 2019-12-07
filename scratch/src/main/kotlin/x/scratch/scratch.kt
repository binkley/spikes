package x.scratch

import lombok.Generated
import kotlin.reflect.typeOf

@Generated // Lie to JaCoCo
@UseExperimental(ExperimentalStdlibApi::class)
fun main() {
    val intType = typeOf<Int>()
    println(intType)

    accessReifiedTypeArg<String>()
    accessReifiedTypeArg<List<String>>()
}

@UseExperimental(ExperimentalStdlibApi::class)
inline fun <reified T> accessReifiedTypeArg() {
    val kType = typeOf<T>()
    println(kType.toString())
}
