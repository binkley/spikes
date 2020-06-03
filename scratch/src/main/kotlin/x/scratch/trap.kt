package x.scratch

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

fun main() {
    println("IT'S A TRAP!")

    val trapReturn = {
        println("THIS DOESN'T LOOK GOOD")
        val day = LocalDate.now().dayOfWeek.getDisplayName(
            TextStyle.FULL_STANDALONE, Locale.ENGLISH
        )
        if (day.endsWith("y")) throw IllegalArgumentException("I DIED")
        else 1
    }.trap<IllegalArgumentException, Int> {
        println("YEP, SOMETHING BAD HAPPENED: $it")
        0
    }
    println(trapReturn)
}

inline fun <reified E : Exception, R> (() -> R).trap(
    action: (E) -> R
): R {
    return try {
        this.invoke()
    } catch (e: Exception) {
        if (e is E) action.invoke(e)
        else throw e
    }
}
