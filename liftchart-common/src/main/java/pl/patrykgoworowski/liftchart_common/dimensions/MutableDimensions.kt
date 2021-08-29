package pl.patrykgoworowski.liftchart_common.dimensions

class MutableDimensions(
    override var start: Float,
    override var top: Float,
    override var end: Float,
    override var bottom: Float,
) : Dimensions {

    val horizontal: Float
        get() = start + end

    val vertical: Float
        get() = top + bottom

    fun set(other: Dimensions) = set(other.start, other.top, other.end, other.bottom)

    fun set(all: Float) = set(all, all, all, all)

    fun set(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ): Dimensions = apply {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }

    fun setLeft(isLTR: Boolean, value: Float): MutableDimensions {
        if (isLTR) start = value
        else end = value
        return this
    }

    fun setRight(isLTR: Boolean, value: Float): MutableDimensions {
        if (isLTR) end = value
        else start = value
        return this
    }

    fun setHorizontal(value: Float): MutableDimensions {
        start = if (value == 0f) value else value / 2
        end = if (value == 0f) value else value / 2
        return this
    }

    fun setVertical(value: Float): MutableDimensions {
        top = if (value == 0f) value else value / 2
        bottom = if (value == 0f) value else value / 2
        return this
    }

    public fun clear() {
        set(0f)
    }

}

fun dimensionsOf(all: Float) = dimensionsOf(all, all, all, all)

fun dimensionsOf(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f,
) = MutableDimensions(start, top, end, bottom)

fun emptyDimensions() = dimensionsOf()