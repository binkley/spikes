package x.scratch

import java.util.Arrays.copyOfRange

/** See https://en.wikipedia.org/wiki/Longest_palindromic_substring */
fun main() {
    println("==PALINDROME")

    println("LONGEST -> ${findLongestPalindrome("")}")
    println("LONGEST -> ${findLongestPalindrome("abcdef")}")
    println("LONGEST -> ${findLongestPalindrome("abcacadzefabdba")}")
}

private fun findLongestPalindrome(s: String): String {
    if (s.isEmpty()) return s
    val boundaries = addBoundaries(s.toCharArray())
    val indicies = IntArray(boundaries.size)
    var c = 0
    var r = 0 // Here the first element in boundaries has been processed
    var m = 0
    // The walking indices to compare if two elements are the same
    var n = 0
    for (i in 1 until boundaries.size) {
        if (i > r) {
            indicies[i] = 0
            m = i - 1
            n = i + 1
        } else {
            val j = c * 2 - i
            if (indicies[j] < r - i - 1) {
                indicies[i] = indicies[j]
                m = -1 // This signals bypassing the while loop below.
            } else {
                indicies[i] = r - i
                n = r + 1
                m = i * 2 - n
            }
        }
        while (m >= 0 && n < boundaries.size && boundaries[m] == boundaries[n]) {
            indicies[i]++
            m--
            n++
        }
        if (i + indicies[i] > r) {
            c = i
            r = i + indicies[i]
        }
    }
    var len = 0
    c = 0
    for (i in 1 until boundaries.size) {
        if (len < indicies[i]) {
            len = indicies[i]
            c = i
        }
    }
    val ss = copyOfRange(boundaries, c - len, c + len + 1)
    return String(removeBoundaries(ss))
}

private fun addBoundaries(cs: CharArray): CharArray {
    if (cs.size == 0) return "||".toCharArray()
    val cs2 = CharArray(cs.size * 2 + 1)
    var i = 0
    while (i < cs2.size - 1) {
        cs2[i] = '|'
        cs2[i + 1] = cs[i / 2]
        i = i + 2
    }
    cs2[cs2.size - 1] = '|'
    return cs2
}

private fun removeBoundaries(cs: CharArray): CharArray {
    if (cs.size < 3) return "".toCharArray()
    val cs2 = CharArray((cs.size - 1) / 2)
    for (i in cs2.indices) {
        cs2[i] = cs[i * 2 + 1]
    }
    return cs2
}
