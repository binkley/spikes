package x.scratch

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import java.lang.System.err
import kotlin.random.Random

private val verbose = true

@BuildParseTree
open class DiceParser : BaseParser<Int>() {
    open fun diceExpression(): Rule = Sequence(
        ZeroOrMore(number()),
        push(matchInt()),
        'd',
        OneOrMore(number()),
        push(roll(pop(), matchInt()))
    )

    open fun number(): Rule = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )

    @Suppress("MemberVisibilityCanBePrivate")
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
    val runner = ReportingParseRunner<Int>(parser.diceExpression())
    val result = runner.run("3d6")

    result.parseErrors.forEach {
        err.println("${it.inputBuffer}: ${it.errorMessage}")
    }
    println(result.resultValue)
}
