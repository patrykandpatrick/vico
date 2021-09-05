package pl.patrykgoworowski.liftchart_common.extension

fun List<() -> Unit>.runEach() {
    forEach { it() }
}