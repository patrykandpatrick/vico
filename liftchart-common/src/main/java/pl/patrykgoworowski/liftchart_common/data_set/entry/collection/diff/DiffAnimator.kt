package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import android.animation.TimeInterpolator

interface DiffAnimator {

    val currentProgress: Float

    var animationInterpolator: TimeInterpolator

    var animationDuration: Long

    fun start(onProgress: (progress: Float) -> Unit)

    fun cancel()

}