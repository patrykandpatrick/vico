package pl.patrykgoworowski.liftchart_common.dimensions

class MutableDimensions<T: Number>(
    override var start: T,
    override var top: T,
    override var end: T,
    override var bottom: T,
) : Dimensions<T> {

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

    fun setRight(isLTR: Boolean, value: T): Dimensions<T> {
        if (isLTR) end = value
        else start = value
        return this
    }

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