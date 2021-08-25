package pl.patrykgoworowski.liftchart_common.extension

private const val ALPHA_BIT_SHIFT = 24
private const val RED_BIT_SHIFT = 16
private const val GREEN_BIT_SHIFT = 8
private const val BLUE_BIT_SHIFT = 0

fun Int.copyColor(
    alpha: Int = this.extractColorChannel(ALPHA_BIT_SHIFT),
    red: Int = this.extractColorChannel(RED_BIT_SHIFT),
    green: Int = this.extractColorChannel(GREEN_BIT_SHIFT),
    blue: Int = this.extractColorChannel(BLUE_BIT_SHIFT),
): Int = (alpha shl ALPHA_BIT_SHIFT) or
        (red shl RED_BIT_SHIFT) or
        (green shl GREEN_BIT_SHIFT) or
        (blue shl BLUE_BIT_SHIFT)

private fun Int.extractColorChannel(bitShift: Int): Int =
    this shr bitShift and 0xff