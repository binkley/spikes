package x.scratch

interface GroupCompanion<T : Group<T>> {
    val ZERO: T
}

interface Group<T : Group<T>> {
    val companion: GroupCompanion<T>

    @Suppress("UNCHECKED_CAST")
    operator fun unaryPlus(): T = this as T
    operator fun unaryMinus(): T
    operator fun plus(addend: T): T
    operator fun minus(subtrahend: T): T = this + -subtrahend
}

interface RingCompanion<T : Ring<T>> : GroupCompanion<T> {
    val ONE: T
}

interface Ring<T : Ring<T>> : Group<T> {
    override val companion: RingCompanion<T>

    operator fun times(multiplicand: T): T
}

interface FieldCompanion<T : Field<T>> : RingCompanion<T>

interface Field<T : Field<T>> : Ring<T> {
    override val companion: FieldCompanion<T>

    fun unaryDiv(): T // Pretend a pseudo-operator

    operator fun div(divisor: T): T = this * divisor.unaryDiv()
}
