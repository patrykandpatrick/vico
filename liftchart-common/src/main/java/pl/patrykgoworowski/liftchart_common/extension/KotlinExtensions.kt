package pl.patrykgoworowski.liftchart_common.extension

inline fun <T> T?.orElse(block: () -> T): T {
    return this ?: block()
}