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
 * *Dice expression syntax*
 * ```
 * [N]'d'D['r'R]['h'K|'l'K][!][+EXP|-EXP...][+A|-A]
 * ```
 * * N - number of dice, default 1
 * * D - sides on the die, or '%' for percentile dice
 * * R - reroll dice this or lower, eg, reroll 1s
 * * K - keep highest ('h') or ('l') lowest rolls
 * * ! - "exploding" dice
 * * EXP - add/subtract more dice expressions
 * * A - add/subtract this fixed amount to the result
 *
 * *Examples*
 *
 * * d6 -- roll 1 6-sided die
 * * 2d%+1 -- roll percentile dice 2 times, sum, and add 1 to the result
 * * 3d6r1! -- roll 3 6-sided dice, rerolling 1s, "explode" the results
 * * 2d4+2d6h1 -- roll 2 4-sided dice, sum; roll 2 6-sided dice keeping the
 *   highest 1; add both results
 *
 * *References*
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
        maybeRerollSome(),
        maybeKeepFewer(),
        maybeExplode(),
        rollTheDice()
    )

    internal open fun rollCount() = Sequence(
        Optional(number()),
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

    internal open fun maybeRerollSome() = Sequence(
        Optional(
            Ch('r'),
            number()
        ),
        push(matchRerollSome())
    )

    internal fun matchRerollSome(): Int {
        val match = match()
        return when {
            match.startsWith('r') -> match.substring(1).toInt()
            else -> 0
        }
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
            else -> peek(2) // 2 down is the die type
        }
    }

    internal open fun maybeExplode() = Sequence(
        Optional(
            Ch('!')
        ),
        push(matchExplode())
    )

    internal fun matchExplode() = if ("!" == match()) 1 else 0

    internal fun rollTheDice(): Boolean {
        val explode = pop() != 0
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
            push(matchInt(0)),
            applyAddOrSubtract(),
            updateRunningTotal()
        )
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
    explode: Boolean,
    random: Random
): Int {
    var total = keep.sum()
    if (explode) keep.forEach {
        total += rollExplosion(it, d, reroll, random)
    }
    return total
}

private fun rollExplosion(
    check: Int,
    d: Int,
    reroll: Int,
    random: Random
): Int {
    var roll = check
    var total = 0
    while (d == roll) {
        roll = rollSpecialDie("!", d, reroll, random)
        total += roll
    }
    return total
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
    showRolls(runner, "6d4l5!")
    showRolls(runner, "3d6+2d4-100")
    showRolls(runner, "d%")
    showRolls(runner, "3d3r1h2!")
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
