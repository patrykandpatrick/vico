package pl.patrykgoworowski.liftchart_common.axis.model

public class MutableDataSetModel(
    override var minX: Float = 0f,
    override var maxX: Float = 0f,
    override var minY: Float = 0f,
    override var maxY: Float = 0f,
) : DataSetModel {

    public fun clear() {
        minX = 0f
        maxX = 0f
        minY = 0f
        maxY = 0f
    }
}
