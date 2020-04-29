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
 * A dice expression has these parts:
 *
 * - 1 or more roll expressions, added/subtracted together
 * - An optional adjustment, added/subtracted at the end
 *
 * The smallest roll expression is just a die type, eg, `d6` meaning roll 1
 * 6-sided die.  See "Examples", below.
 *
 * *Dice expression syntax*
 * ===
 *
 * ```
 * [N]'d'D['r'R]['h'K|'l'K][!|!Z][+EXP|-EXP...][+A|-A]
 * ```
 * * N - number of dice, default 1
 * * D - sides on the die, or '%' for percentile dice
 * * R - reroll dice this or lower, eg, reroll 1s
 * * K - keep highest ('h') or ('l') lowest rolls
 * * Z - "explode" on die face or greater, default is to explode on max die
 *   face
 * * EXP - add/subtract more dice expressions
 * * A - add/subtract this fixed amount to the result
 *
 * *Examples*
 * ===
 *
 * * d6 -- roll 1 6-sided die
 * * 2d%+1 -- roll percentile dice 2 times, sum, and add 1 to the result
 * * 3d6r1! -- roll 3 6-sided dice, rerolling 1s, "explode" on 6s
 * * 3d6r1!5 -- roll 3 6-sided dice, rerolling 1s, "explode" on 5s or 6s
 * * 2d4+2d6h1 -- roll 2 4-sided dice, sum; roll 2 6-sided dice keeping the
 *   highest 1; add both results
 *
 * *Code conventions*
 * ===
 *
 * As each top-level part of a roll expression (eg, die type) parse, a numeric
 * value is pushed onto a stack provided by the parser.  By the end of the
 * roll expression, the stack contains from top down:
 *
 * - Adjustment, or 0 if none specified
 * - Explosion limit, or "die type + 1" if none specified
 * - Dice to keep, or "roll count" if none specified; a positive number is
 *   keep highest, a negative number is keep lowest
 * - Reroll value, or 0 if none specified; rolls of this value or lower are
 *   rerolled
 * - Die type, ie, number of die sides
 * - Roll count, or 1 if none specified; ie, number of dice to roll
 *
 * Evaluating and individual roll expression clears the stack, leaving:
 *
 * - Running total of previous results
 *
 * *References*
 * ===
 *
 * See [roll](https://github.com/matteocorti/roll#examples)
 * See [_Dice Syntax_](https://rollem.rocks/syntax/)
 */
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
        dieType(),
        maybeRerollSome(),
        maybeKeepFewer(),
        maybeExplode(),
        rollTheDice()
    )

    internal open fun rollCount() = Sequence(
        Optional(number()),
        push(matchRollCount())
    )

    internal fun matchRollCount() = matchOrDefault("1").toInt()

    internal open fun number() = Sequence(
        OneOrMore(CharRange('1', '9')),
        ZeroOrMore(CharRange('0', '9'))
    )

    internal open fun dieType() = Sequence(
        Ch('d'),
        FirstOf(
            number(),
            Ch('%')
        ),
        push(matchDieType())
    )

    internal fun matchDieType() = when (val match = match()) {
        "%" -> 100
        else -> match.toInt()
    }

    internal open fun maybeRerollSome() = Sequence(
        Optional(
            Ch('r'),
            number()
        ),
        push(matchRerollSome())
    )

    internal fun matchRerollSome() = when (val match = match()) {
        "" -> 0
        else -> match.substring(1).toInt()
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

    internal fun matchKeepFewer(): Int {
        val match = match()
        return when {
            match.startsWith('h') -> match.substring(1).toInt()
            match.startsWith('l') -> -match.substring(1).toInt()
            else -> peek(2) // roll count
        }
    }

    internal open fun maybeExplode() = Sequence(
        Optional(
            Ch('!'),
            Optional(number())
        ),
        push(matchExplode())
    )

    internal fun matchExplode() = when (val match = match()) {
        "" -> peek(2) + 1 // die type; no exploding
        "!" -> peek(2) // die type; explode on max face
        else -> match.substring(1).toInt()
    }

    internal fun rollTheDice(): Boolean {
        val explode = pop()
        val keep = pop()
        val reroll = pop()
        val dieType = pop()
        val diceCount = pop()
        return push(
            rollDice(diceCount, dieType, reroll, keep, explode, random)
        )
    }

    internal open fun maybeRollMore() = ZeroOrMore(
        Sequence(
            recordAddOrSubtract(),
            rollExpression(),
            applyAddOrSubtract(),
            updateRunningTotal()
        )
    )

    internal open fun recordAddOrSubtract() = Sequence(
        FirstOf(
            Ch('+'),
            Ch('-')
        ),
        push(matchAddOrSubtract())
    )

    internal fun matchAddOrSubtract() = if ("+" == match()) 1 else -1

    internal fun applyAddOrSubtract() = push(pop() * pop())

    internal fun updateRunningTotal() = push(pop() + pop())

    internal open fun maybeAdjust() = Optional(
        Sequence(
            recordAddOrSubtract(),
            number(),
            push(matchAdjustment()),
            applyAddOrSubtract(),
            updateRunningTotal()
        )
    )

    internal fun matchAdjustment() = match().toInt()
}

private fun rollDice(
    n: Int,
    d: Int,
    reroll: Int,
    keep: Int,
    explode: Int,
    random: Random
): Int {
    val rolls = (1..n).map {
        rollSpecialDie("", d, reroll, random)
    }.toMutableList()

    rolls.sort()

    val kept: List<Int> =
        if (keep < 0) keepLowest(rolls, n, keep)
        else keepHighest(rolls, n, keep)

    return rollExplosions(kept, d, reroll, explode, random)
}

private fun keepLowest(rolls: List<Int>, n: Int, keep: Int): List<Int> {
    if (verbose) rolls.subList(-keep, n).forEach {
        println("drop -> $it")
    }
    return rolls.subList(0, -keep)
}

private fun keepHighest(rolls: List<Int>, n: Int, keep: Int): List<Int> {
    if (verbose) rolls.subList(0, n - keep).forEach {
        println("drop -> $it")
    }
    return rolls.subList(n - keep, n)
}

private fun rollSpecialDie(
    prefix: String,
    d: Int,
    reroll: Int,
    random: Random
): Int {
    var roll = rollDie(d, random)
    if (verbose) println("${prefix}roll(d$d) -> $roll")
    while (roll <= reroll) {
        roll = rollDie(d, random)
        if (verbose) println("${prefix}reroll(d$d) -> $roll")
    }
    return roll
}

private fun rollExplosions(
    keep: List<Int>,
    d: Int,
    reroll: Int,
    explode: Int,
    random: Random
): Int {
    var total = keep.sum()
    keep.forEach {
        var roll = it
        while (roll >= explode) {
            roll = rollExplosion(d, reroll, random)
            total += roll
        }
    }
    return total
}

private fun rollExplosion(d: Int, reroll: Int, random: Random) =
    rollSpecialDie("!", d, reroll, random)

private fun rollDie(d: Int, random: Random) =
    random.nextInt(0, d) + 1

fun main() {
    verbose = true

    val rule = Parboiled.createParser(DiceParser::class.java).diceExpression()
    val runner = RecoveringParseRunner<Int>(rule)

    showRolls(runner, "3d6")
    showRolls(runner, "3d6+1")
    showRolls(runner, "3d6-1")
    showRolls(runner, "10d3!")
    showRolls(runner, "10d3!2")
    showRolls(runner, "4d6h3")
    showRolls(runner, "4d6l3")
    showRolls(runner, "3d6+2d4")
    showRolls(runner, "d%")
    showRolls(runner, "6d4l5!")
    showRolls(runner, "3d3r1h2!")
}

private fun showRolls(
    runner: RecoveringParseRunner<Int>,
    expression: String
) {
    println("---")
    println("Rolling $expression")
    val result = runner.run(expression)
    result.parseErrors.forEach {
        err.println(printParseError(it))
    }
    if (!result.hasErrors())
        println("RESULT -> ${result.resultValue}")
}
