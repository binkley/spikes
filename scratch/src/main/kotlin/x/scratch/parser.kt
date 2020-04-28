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

internal var verbose = false

/**
 * See [roll](https://github.com/matteocorti/roll#examples)
 * See [_Dice Syntax_](https://rollem.rocks/syntax/)
 * */
@BuildParseTree
open class DiceParser(
    private val random: Random = Random.Default
) : BaseParser<Int>() {
    open fun diceExpression(): Rule = Sequence(
        rollExpression(),
        maybeRollMore(),
        maybeAdjust()
    )

    internal open fun rollExpression() = Sequence(
        rollCount(),
        Ch('d'),
        dieType(),
        maybeKeepFewer(),
        maybeExplode(),
        rollTheDice()
    )

    internal open fun rollCount() = Sequence(
        ZeroOrMore(number()),
        push(matchInt(1))
    )

    internal open fun number() = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )

    internal fun matchInt(default: Int) =
        matchOrDefault(default.toString()).toInt()

    internal open fun dieType() = Sequence(
        FirstOf(
            OneOrMore(number()),
            Ch('%')
        ),
        push(matchDieType())
    )

    internal fun matchDieType() = when (val match = match()) {
        "%" -> 100
        else -> match.toInt()
    }

    internal open fun maybeKeepFewer() = Sequence(
        Optional(
            FirstOf(
                Ch('h'),
                Ch('l')
            ),
            number()
        ),
        push(matchKeepFewer())
    )

    internal open fun matchKeepFewer(): Int {
        val match = match()
        return when {
            match.startsWith('h') -> match.substring(1).toInt()
            match.startsWith('l') -> -match.substring(1).toInt()
            else -> peek(1)
        }
    }

    internal open fun maybeExplode() = Sequence(
        Optional(
            Ch('!')
        ),
        push(matchExplode())
    )

    internal fun matchExplode() = if (match() == "") 0 else 1

    internal fun rollTheDice(): Boolean {
        val explode = pop() != 0
        val keep = pop()
        val dieType = pop()
        val diceCount = pop()
        return push(rollDice(diceCount, dieType, 0, keep, explode, random))
    }

    internal open fun carrySign() = push(
        when (match()) {
            "+" -> 1
            else -> -1
        }
    )

    internal open fun signNumber() = push(pop() * pop())

    internal open fun maybeRollMore() = ZeroOrMore(
        Sequence(
            FirstOf(
                Ch('+'),
                Ch('-')
            ),
            carrySign(),
            rollExpression(),
            signNumber(),
            keepAddingDice()
        )
    )

    internal fun keepAddingDice() = push(pop() + pop())

    internal open fun maybeAdjust() = Sequence(
        Optional(
            FirstOf(
                Ch('+'),
                Ch('-')
            ),
            carrySign(),
            number(),
            signNumber()
        ),
        push(pop() + matchInt(0))
    )
}

private fun rollDice(
    n: Int,
    d: Int,
    reroll: Int,
    keep: Int,
    explode: Boolean,
    random: Random
): Int {
    fun rollSpecialDie() = rollDie(d, random)

    val rolls = ArrayList<Int>(n)
    for (i in 1..n) {
        var roll = rollSpecialDie()
        while (roll <= reroll) {
            if (verbose) println("*roll(d$d) -> $roll")
            roll = rollSpecialDie()
        }
        if (verbose) println("roll(d$d) -> $roll")
        rolls += roll

        if (explode) while (roll == d) {
            roll = rollSpecialDie()
            while (roll <= reroll) {
                if (verbose) println("*roll(d$d) -> $roll")
                roll = rollSpecialDie()
            }
            if (verbose) println("!roll(d$d) -> $roll")
            rolls += roll
        }
    }

    if (n == keep) return rolls.sum()

    rolls.sort()

    return if (keep < 0) {
        if (verbose) rolls.subList(-keep, rolls.size).forEach {
            println("drop -> $it")
        }
        rolls.subList(0, -keep).sum()
    } else {
        if (verbose) rolls.subList(0, n - keep).forEach {
            println("drop -> $it")
        }
        rolls.subList(n - keep, rolls.size).sum()
    }
}

private fun rollDie(d: Int, random: Random) =
    random.nextInt(0, d) + 1

fun main() {
    verbose = true

    val rule = Parboiled.createParser(DiceParser::class.java).diceExpression()
    val runner = RecoveringParseRunner<Int>(rule)

    showRolls(runner, "3d3!+100")
    showRolls(runner, "3d6")
    showRolls(runner, "4d6h3")
    showRolls(runner, "4d6l3")
    showRolls(runner, "3d6+2d4-100")
    showRolls(runner, "d%")
}

private fun showRolls(
    runner: RecoveringParseRunner<Int>,
    expression: String
) {
    println("---")
    val result = runner.run(expression)
    result.parseErrors.forEach {
        err.println(printParseError(it))
    }
    if (!result.hasErrors())
        println("$expression -> ${result.resultValue}")
}
