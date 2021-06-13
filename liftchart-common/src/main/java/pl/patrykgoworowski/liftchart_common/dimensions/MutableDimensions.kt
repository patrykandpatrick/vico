package pl.patrykgoworowski.liftchart_common.dimensions

class MutableDimensions(
    override var start: Float,
    override var top: Float,
    override var end: Float,
    override var bottom: Float,
) : Dimensions {

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

    fun setLeft(isLTR: Boolean, value: Float): Dimensions {
        if (isLTR) start = value
        else end = value
        return this
    }

    fun setRight(isLTR: Boolean, value: Float): Dimensions {
        if (isLTR) end = value
        else start = value
        return this
    }

    fun setHorizontal(value: Float): Dimensions {
        start = value
        end = value
        return this
    }

    fun setVertical(value: Float): Dimensions {
        top = value
        bottom = value
        return this
    }

}