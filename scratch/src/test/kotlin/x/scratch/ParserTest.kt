package x.scratch

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.parboiled.Parboiled.createParser
import org.parboiled.parserunners.RecoveringParseRunner
import java.util.stream.Stream
import kotlin.random.Random

@TestInstance(PER_CLASS)
internal class ParserTest {
    @MethodSource("args")
    @ParameterizedTest
    fun `should parse`(expression: String, result: Int) {
        val random = Random(1L)
        val rule =
            createParser(DiceParser::class.java, random).diceExpression()
        val runner = RecoveringParseRunner<Int>(rule)

        expect(runner.run(expression).resultValue).toBe(result)
    }

    internal companion object {
        @JvmStatic
        fun args() = Stream.of(
            Arguments.of("3d6", 10),
            Arguments.of("10d3!", 20),
            Arguments.of("4d6h3", 10),
            Arguments.of("4d6l3", 6),
            Arguments.of("3d6+2d4", 17),
            Arguments.of("d%", 66),
            Arguments.of("6d4l5!", 20),
            Arguments.of("3d3r1h2!", 10),
        )
    }
}
