package x.scratch.units.english

import x.scratch.units.FiniteBigRational
import x.scratch.units.FiniteBigRational.Companion.ONE
import x.scratch.units.Measure
import x.scratch.units.Units
import x.scratch.units.minus
import x.scratch.units.over
import x.scratch.units.plus
import x.scratch.units.times

sealed class EnglishLengths<U : EnglishLengths<U>>(name: String) :
    Units<U>(name)

object Poppyseeds : EnglishLengths<Poppyseeds>("Poppyseed") {
    override fun new(value: FiniteBigRational) = Poppyseed(value)
    override fun format(value: FiniteBigRational) = "$value poppyseeds"
}

class Poppyseed(value: FiniteBigRational) :
    Measure<Poppyseeds>(Poppyseeds, value)

object Barleycorns : EnglishLengths<Barleycorns>("Barleycorn") {
    override fun new(value: FiniteBigRational) = Barleycorn(value)
    override fun format(value: FiniteBigRational) = "$value barleycorns"
}

class Barleycorn(value: FiniteBigRational) :
    Measure<Barleycorns>(Barleycorns, value)

object Inches : EnglishLengths<Inches>("Inch") {
    override fun new(value: FiniteBigRational) = Inch(value)
    override fun format(value: FiniteBigRational) = "$value\""
}

class Inch(value: FiniteBigRational) :
    Measure<Inches>(Inches, value)

/** There is probably a clever way to do this, but this is simple. */
private val rates = mapOf(
    (Poppyseeds to Poppyseeds) to ONE,
    (Poppyseeds to Barleycorns) to (4 over 1),
    (Poppyseeds to Inches) to (12 over 1),
    (Barleycorns to Poppyseeds) to (1 over 4),
    (Barleycorns to Barleycorns) to ONE,
    (Barleycorns to Inches) to (3 over 1),
    (Inches to Poppyseeds) to (1 over 12),
    (Inches to Barleycorns) to (1 over 3),
    (Inches to Inches) to ONE
)

fun <U : EnglishLengths<U>, V : EnglishLengths<V>> Measure<U>.to(other: V) =
    other.new(value * (rates[other to unit] ?: error("Missing rate")))

operator fun <U : EnglishLengths<U>, V : EnglishLengths<V>> Measure<U>.plus(
    other: Measure<V>
) = unit.new(value + other.to(unit).value)

operator fun <U : EnglishLengths<U>, V : EnglishLengths<V>> Measure<U>.minus(
    other: Measure<V>
) = unit.new(value - other.to(unit).value)
