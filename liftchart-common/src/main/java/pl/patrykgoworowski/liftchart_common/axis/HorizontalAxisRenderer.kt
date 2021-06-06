package pl.patrykgoworowski.liftchart_common.axis


interface HorizontalAxisRenderer : AxisRenderer<HorizontalAxisPosition> {

    fun getHeight(position: HorizontalAxisPosition): Int

}