package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class DefaultDiffAnimator(
    animationDuration: Long = DEFAULT_ANIM_DURATION,
    animationInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
) : DiffAnimator {

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = animationDuration
        interpolator = animationInterpolator

        addUpdateListener {
            onProgress?.invoke(it.animatedFraction)
        }
        addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator?) {
                onProgress = null
            }

            override fun onAnimationEnd(animation: Animator?) {
                onProgress = null
            }
        })
    }

    private var onProgress: ((progress: Float) -> Unit)? = null

    override val currentProgress: Float
        get() = valueAnimator.animatedFraction

    override var animationInterpolator: TimeInterpolator by valueAnimator::interpolator

    override var animationDuration: Long by valueAnimator::duration

    override fun start(onProgress: (progress: Float) -> Unit) {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        this.onProgress = onProgress
        valueAnimator.start()
    }

    override fun cancel() {
        valueAnimator.cancel()
        onProgress = null
    }

    companion object {
        const val DEFAULT_ANIM_DURATION = 250L
    }

}