package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import android.animation.TimeInterpolator

public interface DiffAnimator {

    public val currentProgress: Float

    public var animationInterpolator: TimeInterpolator

    public var animationDuration: Long

    public fun start(onProgress: (progress: Float) -> Unit)

    public fun cancel()

}