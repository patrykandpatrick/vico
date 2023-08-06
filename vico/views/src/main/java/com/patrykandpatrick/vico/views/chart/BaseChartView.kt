/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.views.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.context.MutableCartesianMeasureContext
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.views.extension.density
import com.patrykandpatrick.vico.views.extension.dpInt
import com.patrykandpatrick.vico.views.extension.fontScale
import com.patrykandpatrick.vico.views.extension.isAttachedToWindowCompat
import com.patrykandpatrick.vico.views.extension.isLtr
import com.patrykandpatrick.vico.views.extension.measureDimension
import com.patrykandpatrick.vico.views.extension.specSize
import com.patrykandpatrick.vico.views.extension.verticalPadding

/**
 * The base for [View]s that display a chart. Subclasses define a [Model] implementation they can handle.
 */
public abstract class BaseChartView<Model> internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    protected val contentBounds: RectF = RectF()

    protected val measureContext: MutableCartesianMeasureContext = MutableCartesianMeasureContext(
        canvasBounds = contentBounds,
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr,
        isHorizontalScrollEnabled = false,
        chartScale = 1f,
    )

    protected val animator: ValueAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = Animation.DIFF_DURATION.toLong()
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { progressModelOnAnimationProgress(it.animatedFraction) }
        }

    protected open val updateListener: () -> Unit = {
        if (model != null || runInitialAnimation) {
            handler.post(animator::start)
        } else {
            progressModelOnAnimationProgress(progress = Animation.range.endInclusive)
        }
    }

    /**
     * The [Model] containing data to render a chart.
     */
    public abstract val model: Model?

    /**
     * A [ChartModelProducer] can provide [Model] updates asynchronously.
     *
     * @see ChartModelProducer
     */
    public var entryProducer: ChartModelProducer<Model>? = null
        set(value) {
            if (field === value) return
            field?.unregisterFromUpdates(key = this)
            field = value
            if (isAttachedToWindowCompat) registerForUpdates()
        }

    /**
     * Whether to display an animation when the chart is created. In this animation, the value of each chart entry is
     * animated from zero to the actual value.
     */
    public var runInitialAnimation: Boolean = true

    protected open fun registerForUpdates() {
        entryProducer?.registerForUpdates(
            key = this,
            updateListener = updateListener,
            getOldModel = { model },
        ) { model ->
            post {
                setModel(model = model)
                postInvalidateOnAnimation()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (entryProducer?.isRegistered(key = this) != true) registerForUpdates()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        entryProducer?.unregisterFromUpdates(key = this)
    }

    /**
     * Sets the [Model] for this [BaseChartView]’s chart.
     */
    public abstract fun setModel(model: Model)

    private fun progressModelOnAnimationProgress(progress: Float) {
        entryProducer?.progressModel(this, progress)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> DefaultDimens.CHART_HEIGHT.dpInt + verticalPadding
            MeasureSpec.AT_MOST -> minOf(
                DefaultDimens.CHART_HEIGHT.dpInt + verticalPadding,
                heightMeasureSpec.specSize,
            )

            else -> measureDimension(heightMeasureSpec.specSize, heightMeasureSpec)
        }

        setMeasuredDimension(width, height)

        contentBounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom,
        )
    }

    /**
     * Sets the duration (in milliseconds) of difference animations.
     */
    public fun setDiffAnimationDuration(durationMillis: Long) {
        animator.duration = durationMillis
    }

    /**
     * Sets the [Interpolator] for difference animations.
     */
    public fun setDiffAnimationInterpolator(interpolator: Interpolator) {
        animator.interpolator = interpolator
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        // This function may be invoked inside of the View’s constructor, before the measureContext is initialized.
        // In this case, we can ignore this callback, as the layout direction will be determined when the MeasureContext
        // instance is created.
        measureContext?.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    }
}
