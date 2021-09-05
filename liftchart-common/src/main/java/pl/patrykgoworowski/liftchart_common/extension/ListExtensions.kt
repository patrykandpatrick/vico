package pl.patrykgoworowski.liftchart_common.extension

fun List<() -> Unit>.runEach() {
    forEach { it() }
}

fun <T> List<(T) -> Unit>.runEach(argument: T) {
    forEach { it(argument) }
}