package x.domainpersistencemodeling

import lombok.Generated

/**
 * Turns a unary function into a binary function that applies only the first
 * argument.
 */
@Generated // TODO: No longer used?
fun <T, R> ((T) -> R).uncurryFirst(): (T, Any?) -> R {
    return { it: T, _: Any? -> invoke(it) }
}

/**
 * Turns a unary function into a binary function that applies only the second
 * argument.
 */
fun <U, R> ((U) -> R).uncurrySecond(): (Any?, U) -> R {
    return { _: Any?, it: U -> invoke(it) }
}
