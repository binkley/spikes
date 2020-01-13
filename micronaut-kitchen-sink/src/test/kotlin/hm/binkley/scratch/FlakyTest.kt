package hm.binkley.scratch

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

private var n = 1

internal class FlakyTest : StringSpec({
    "corn flakes".config(enabled = false) {
        n++ % 3 shouldBe 0
    }
})
