package pl.patrykgoworowski.liftchart_common

class IllegalPercentageException(percentage: Int) : IllegalArgumentException(
    "Expected a percentage (0-100), got $percentage."
)
