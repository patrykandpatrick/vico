package pl.patrykgoworowski.liftchart_common.extension

import android.graphics.PointF
import android.view.MotionEvent

val MotionEvent.pointF: PointF
    get() = PointF(x, y)
