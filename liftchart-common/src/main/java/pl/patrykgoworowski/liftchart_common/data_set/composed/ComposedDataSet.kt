package pl.patrykgoworowski.liftchart_common.data_set.composed

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.getClosestMarkerEntryPositionModel
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.extension.updateAll
import pl.patrykgoworowski.liftchart_common.marker.Marker
import java.util.*

class ComposedDataSet<Model : EntryModel>(
    dataSets: List<DataSet<Model>>
) : DataSet<ComposedEntryModel<Model>> {

    public val dataSets = ArrayList(dataSets)

    private val tempAxisModel = MutableDataSetModel()
    private val segmentProperties = MutableSegmentProperties()

    override val bounds: RectF = RectF()
    override val markerLocationMap = TreeMap<Float, MutableList<Marker.EntryModel>>()

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        this.bounds.set(left, top, right, bottom)
        dataSets.forEach { dataSet -> dataSet.setBounds(left, top, right, bottom) }
    }

    override var minY: Float? = null
    override var maxY: Float? = null
    override var minX: Float? = null
    override var maxX: Float? = null
    override var isHorizontalScrollEnabled: Boolean = false
        set(value) {
            field = value
            dataSets.forEach { dataSet -> dataSet.isHorizontalScrollEnabled = value }
        }
    override var zoom: Float? = null
        set(value) {
            field = value
            dataSets.forEach { dataSet -> dataSet.zoom = value }
        }

    override val maxScrollAmount: Float
        get() = dataSets.maxOf { it.maxScrollAmount }

    override fun draw(
        canvas: Canvas,
        model: ComposedEntryModel<Model>,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
        marker: Marker?
    ) {
        val (touchPoint) = rendererViewState
        markerLocationMap.clear()
        model.forEachModelWithDataSet { _, item, dataSet ->
            dataSet.draw(canvas, item, segmentProperties, rendererViewState, null)
            markerLocationMap.updateAll(dataSet.markerLocationMap)
        }
        if (touchPoint != null && marker != null) {
            markerLocationMap.getClosestMarkerEntryPositionModel(touchPoint)?.let { markerModel ->
                marker.draw(canvas, bounds, markerModel)
            }
        }
    }

    override fun getMeasuredWidth(model: ComposedEntryModel<Model>): Int {
        var result = 0
        model.forEachModelWithDataSet { _, item, dataSet ->
            result = maxOf(dataSet.getMeasuredWidth(item), result)
        }
        return result
    }

    override fun getSegmentProperties(model: ComposedEntryModel<Model>): SegmentProperties {
        segmentProperties.clear()
        model.forEachModelWithDataSet { _, item, dataSet ->
            val dataSetProps = dataSet.getSegmentProperties(item)
            segmentProperties.apply {
                contentWidth = maxOf(contentWidth, dataSetProps.contentWidth)
                marginWidth = maxOf(marginWidth, dataSetProps.marginWidth)
            }
        }
        return segmentProperties
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: ComposedEntryModel<Model>) {
        axisModel.clear()
        tempAxisModel.clear()
        model.forEachModelWithDataSet { index, item, dataSet ->
            dataSet.setToAxisModel(tempAxisModel, item)
            axisModel.apply {
                minX = if (index == 0) tempAxisModel.minX else minOf(minX, tempAxisModel.minX)
                maxX = if (index == 0) tempAxisModel.maxX else maxOf(maxX, tempAxisModel.maxX)
                minY = if (index == 0) tempAxisModel.minY else minOf(minY, tempAxisModel.minY)
                maxY = if (index == 0) tempAxisModel.maxY else maxOf(maxY, tempAxisModel.maxY)
            }
        }
    }

    private inline fun ComposedEntryModel<Model>.forEachModelWithDataSet(
        action: (index: Int, item: Model, dataSet: DataSet<Model>) -> Unit
    ) {
        val minSize = minOf(composedEntryCollections.size, dataSets.size)
        for (index in 0 until minSize) {
            action(index, composedEntryCollections[index], dataSets[index])
        }
    }
}
