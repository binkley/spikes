package x.scratch

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.random.Random

@BuildParseTree
open class DiceParser : BaseParser<Int>() {
    open fun inputLine(): Rule = Sequence(
        diceExpression(),
        EOI,
        push(roll(pop() as Int, pop() as Int))
    )

    open fun diceExpression(): Rule = Sequence(
        ZeroOrMore(number()),
        push(3),
        'd',
        OneOrMore(number()),
        push(6)
    )

    open fun number(): Rule = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )
}

private fun roll(n: Int, d: Int): Int {
    var total = 0
    for (i in 1..n)
        total += Random.nextInt(0, d) + 1
    return total
}

fun main() {
    val parser = Parboiled.createParser(DiceParser::class.java)
    val result = ReportingParseRunner<Int>(parser.inputLine()).run("3d6")
    println(result.matched)
    println(result.resultValue)
    println(result.hasErrors())
    println(result.parseErrors)
}
