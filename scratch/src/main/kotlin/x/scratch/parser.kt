package x.scratch

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import java.lang.System.err
import kotlin.random.Random

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
    for (i in 1..n)
        total += Random.nextInt(0, d) + 1
    return total
}

fun main() {
    val parser = Parboiled.createParser(DiceParser::class.java)
    val result =
        ReportingParseRunner<Int>(parser.diceExpression()).run("d2")
    result.parseErrors.forEach { err.println("${it.inputBuffer}: ${it.errorMessage}") }
    println(result.resultValue)
}
