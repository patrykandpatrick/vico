package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import android.animation.TimeInterpolator

public class TestDiffAnimator : DiffAnimator {

    private var onProgress: ((progress: Float) -> Unit)? = null
    override var currentProgress: Float = 0f
    override lateinit var animationInterpolator: TimeInterpolator

    override var animationDuration: Long = 0

    override fun start(onProgress: (progress: Float) -> Unit) {
        this.onProgress = onProgress
        onProgress(currentProgress)
    }

    fun updateProgress(progress: Float) {
        onProgress?.invoke(progress)
    }

    override fun cancel() {}
}