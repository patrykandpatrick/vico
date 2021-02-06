package pl.patrykgoworowski.liftchart_common.extension

fun <T> ArrayList<T>.getOrDefault(index: Int, getDefault: () -> T): T =
    getOrNull(index) ?: getDefault().also { add(it) }

fun <T> ArrayList<T>.getRepeatingOrDefault(index: Int, getDefault: () -> T) =
    getOrNull(index) ?: getOrNull(index % size.coerceAtLeast(1)) ?: getDefault().also { add(it) }

public fun <T> ArrayList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}