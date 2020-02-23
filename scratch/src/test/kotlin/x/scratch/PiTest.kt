package x.scratch

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

/**
 * See <a href="http://www.geom.uiuc.edu/~huberty/math5337/groupe/digits.html"><cite>100,000 Digits of Pi</cite></a>
 */
internal class PiTest {
    @Test
    fun `test me`() {
        val pi = Pi.generatePiDigits()
        val digits = mutableListOf<Int>()
        pi.take(154).forEach {
            digits += it
        }
        val str = digits.joinToString("")
        val many =
            "3141592653589793238462643383279502884197169399375105820974944592307816406286208998628034825342117067982148086513282306647093844609550582231725359408128481"

        expect(str).toBe(many)
    }
}
