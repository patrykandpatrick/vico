package pl.patrykgoworowski.liftchart_common.dimensions

data class Dimensions<T: Number>(
    var start: T,
    var top: T,
    var end: T,
    var bottom: T
) {

    fun set(all: T) = set(all, all, all, all)

    fun set(
        start: T,
        top: T,
        end: T,
        bottom: T,
    ): Dimensions<T> = apply {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }

    fun setLeft(isLTR: Boolean, value: T): Dimensions<T> {
        if (isLTR) start = value
        else end = value
        return this
    }

    fun getLeft(isLTR: Boolean) = if (isLTR) start else end

    fun setRight(isLTR: Boolean, value: T): Dimensions<T> {
        if (isLTR) end = value
        else start = value
        return this
    }

    fun getRight(isLTR: Boolean) = if (isLTR) end else start

    fun setHorizontal(value: T): Dimensions<T> {
        start = value
        end = value
        return this
    }

    fun setVertical(value: T): Dimensions<T> {
        top = value
        bottom = value
        return this
    }

}

fun floatDimensions(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f
) = Dimensions(start, top, end, bottom)