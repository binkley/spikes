@file:Suppress("MemberVisibilityCanBePrivate")

package x.scratch

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.errors.ErrorUtils.printParseError
import org.parboiled.parserunners.RecoveringParseRunner
import java.lang.System.err
import kotlin.random.Random

private val verbose = true

@BuildParseTree
open class DiceParser : BaseParser<Int>() {
    open fun diceExpression(): Rule = Sequence(
        rollCount(),
        'd',
        dieType(),
        rollTheDice()
    )

    internal fun rollTheDice(): Boolean {
        swap()
        return push(roll(pop(), pop()))
    }

    internal open fun rollCount() = Sequence(
        ZeroOrMore(number()),
        push(matchInt())
    )

    internal open fun dieType() = Sequence(
        OneOrMore(number()),
        push(matchInt())
    )

    internal open fun number(): Rule = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )

    internal fun matchInt() = (matchOrDefault("1")).toInt()
}

private fun roll(n: Int, d: Int): Int {
    var total = 0
    for (i in 1..n) {
        val roll = Random.nextInt(0, d) + 1
        if (verbose) println("roll -> $roll")
        total += roll
    }
    return total
}

fun main() {
    val parser = Parboiled.createParser(DiceParser::class.java)
    val runner = RecoveringParseRunner<Int>(parser.diceExpression())
    val result = runner.run("3d6")

    result.parseErrors.forEach {
        err.println(printParseError(it))
    }
    if (!result.hasErrors())
        println(result.resultValue)
}
