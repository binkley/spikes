package x.scratch

/**
 * See https://medium.com/@elizarov/deep-recursion-with-coroutines-7c53e15993e3
 * See https://blog.jetbrains.com/kotlin/2020/07/kotlin-1-4-rc-debugging-coroutines/?ref=codebldr
 */
fun main() {
    println("==DEEP RECURSION WITH COROUTINES")

    val n = 100_000
    val deepTree = generateSequence(Tree(null, null)) { prev ->
        Tree(prev, null)
    }.take(n).last()

    println("DEPTH -> ${depth(deepTree)}")
}

private class Tree(val left: Tree?, val right: Tree?)

@OptIn(ExperimentalStdlibApi::class)
private val depthFunction = DeepRecursiveFunction<Tree?, Int> { t ->
    if (t == null) 0 else maxOf(
        callRecursive(t.left),
        callRecursive(t.right)
    ) + 1
}

@OptIn(ExperimentalStdlibApi::class)
private fun depth(t: Tree) = depthFunction(t)
