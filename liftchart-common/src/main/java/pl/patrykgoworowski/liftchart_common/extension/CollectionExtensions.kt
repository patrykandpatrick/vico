package pl.patrykgoworowski.liftchart_common.extension

import pl.patrykgoworowski.liftchart_common.constants.ERR_REPEATING_COLLECTION_EMPTY
import kotlin.math.abs

fun <T> ArrayList<T>.getOrDefault(index: Int, getDefault: () -> T): T =
    getOrNull(index) ?: getDefault().also { add(it) }

fun <T> ArrayList<T>.getRepeatingOrDefault(index: Int, getDefault: () -> T) =
    getOrNull(index) ?: getOrNull(index % size.coerceAtLeast(1)) ?: getDefault().also { add(it) }

fun <T> List<T>.getRepeating(index: Int): T {
    if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
    return get(index % size.coerceAtLeast(1))
}

public fun <T> ArrayList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}

public inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

public inline fun <T> Iterable<T>.forEachIndexedExtended(
    selector: (index: Int, isFirst: Boolean, isLast: Boolean, value: T) -> Unit
) {
    var index = 0
    val iterator = iterator()
    var next: T
    while (iterator.hasNext()) {
        next = iterator.next()
        selector(index, index == 0, !iterator.hasNext(), next)
        index++
    }
}

fun Collection<Float>.findClosestPositiveValue(value: Float): Float? {
    if (isEmpty()) return null
    var closestValue: Float? = null
    forEach { checkedValue ->
        closestValue = when {
            closestValue == null -> checkedValue
            abs(closestValue!! - value) > abs(checkedValue - value) -> checkedValue
            else -> closestValue
        }
    }
    return closestValue
}