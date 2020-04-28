@file:Suppress("MemberVisibilityCanBePrivate")

package x.scratch

import org.parboiled.BaseParser
import org.parboiled.Parboiled
import org.parboiled.Rule
import org.parboiled.annotations.BuildParseTree
import org.parboiled.errors.ErrorUtils.printParseError
import org.parboiled.parserunners.RecoveringParseRunner
import org.parboiled.support.ParsingResult
import java.lang.System.err
import kotlin.random.Random

private val verbose = true

/**
 * See [roll](https://github.com/matteocorti/roll#examples)
 * See [_Dice Syntax_](https://rollem.rocks/syntax/)
 * */
@BuildParseTree
open class DiceParser : BaseParser<Int>() {
    open fun diceExpression(): Rule = Sequence(
        rollCount(),
        Ch('d'),
        dieType(),
        maybeExplode(),
        maybeAdjust(),
        rollTheDice()
    )

    internal fun rollTheDice(): Boolean {
        val adjust = pop()
        val explode = pop() != 0
        val dieType = pop()
        val diceCount = pop()
        return push(rollDice(diceCount, dieType, explode) + adjust)
    }

    internal open fun rollCount() = Sequence(
        ZeroOrMore(number()),
        push(matchInt(1))
    )

    internal open fun dieType() = Sequence(
        FirstOf(
            OneOrMore(number()),
            Ch('%')
        ),
        push(matchDieType())
    )

    internal open fun maybeExplode() = Sequence(
        Optional(
            Ch('!')
        ),
        push(matchExplode())
    )

    internal open fun maybeAdjust() = Sequence(
        Optional(
            FirstOf(
                Ch('+'),
                Ch('-')
            ),
            number()
        ),
        push(matchInt(0))
    )

    internal open fun number(): Rule = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )

    internal fun matchInt(default: Int) =
        (matchOrDefault(default.toString())).toInt()

    internal fun matchDieType() = when (val match = match()) {
        "%" -> 100
        else -> match.toInt()
    }

    internal fun matchExplode() = if (match() == "") 0 else 1
}

private fun rollDice(
    n: Int,
    d: Int,
    explode: Boolean
): Int {
    var total = 0
    for (i in 1..n) {
        var roll = rollDie(d)
        if (verbose) println("roll -> $roll")
        total += roll

        if (explode) while (roll == d) {
            roll = rollDie(d)
            if (verbose) println("!roll -> $roll")
            total += roll
        }
    }

    return total
}

private fun rollDie(d: Int) = Random.nextInt(0, d) + 1

fun main() {
    val parser = Parboiled.createParser(DiceParser::class.java)
    val runner = RecoveringParseRunner<Int>(parser.diceExpression())

    showRolls(runner.run("3d3!+100"))
    showRolls(runner.run("3d6"))
    showRolls(runner.run("d%"))
}

private fun showRolls(result: ParsingResult<Int>) {
    result.parseErrors.forEach {
        err.println(printParseError(it))
    }
    if (!result.hasErrors())
        println(result.resultValue)
}
