package pl.patrykgoworowski.liftchart_common.axis.model

class MutableDataSetModel(
    override var minX: Float = 0f,
    override var maxX: Float = 0f,
    override var minY: Float = 0f,
    override var maxY: Float = 0f,
) : DataSetModel