/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.common.Animation
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import com.patrykandpatrick.vico.core.common.MutableMeasureContext
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.extension.set
import com.patrykandpatrick.vico.core.common.extension.spToPx
import com.patrykandpatrick.vico.views.common.extension.defaultColors
import com.patrykandpatrick.vico.views.common.extension.density
import com.patrykandpatrick.vico.views.common.extension.isLtr
import com.patrykandpatrick.vico.views.common.extension.specSize
import com.patrykandpatrick.vico.views.common.extension.start
import com.patrykandpatrick.vico.views.common.extension.verticalPadding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * Displays a [CartesianChart].
 */
public abstract class BaseChartView<Model>
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        protected val contentBounds: RectF = RectF()

        protected open val measureContext: MutableMeasureContext =
            MutableMeasureContext(
                canvasBounds = contentBounds,
                density = context.density,
                isLtr = context.isLtr,
                spToPx = context::spToPx,
            )

        /**
         * The chart model.
         */
        public var model: Model? = null
            protected set

        protected val animator: ValueAnimator =
            ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
                duration = Animation.DIFF_DURATION.toLong()
                interpolator = FastOutSlowInInterpolator()
            }

        protected val extraStore: MutableExtraStore = MutableExtraStore()

        protected var coroutineScope: CoroutineScope? = null

        protected var animationFrameJob: Job? = null

        protected var finalAnimationFrameJob: Job? = null

        protected var isAnimationRunning: Boolean = false

        protected var isAnimationFrameGenerationRunning: Boolean = false

        protected var placeholder: View? = null

        /**
         * Whether to display an animation when the chart is created. In this animation, the value of each chart entry
         * is animated from zero to the actual value.
         */
        public var runInitialAnimation: Boolean = true

        /**
         * The [CoroutineDispatcher] to be used for the handling of difference animations.
         */
        public var dispatcher: CoroutineDispatcher = Dispatchers.Default

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            coroutineScope?.cancel()
            coroutineScope = null
            animator.cancel()
            isAnimationRunning = false
        }

        /**
         * The color of elevation overlays, which are applied to [ShapeComponent]s that cast shadows.
         */
        public var elevationOverlayColor: Int = context.defaultColors.elevationOverlayColor.toInt()

        override fun addView(
            child: View,
            index: Int,
            params: ViewGroup.LayoutParams?,
        ) {
            check(childCount == 0) { "Only one placeholder can be added." }
            super.addView(child, index, params)
            placeholder = child
            updatePlaceholderVisibility()
        }

        override fun onViewRemoved(child: View?) {
            super.onViewRemoved(child)
            placeholder = null
        }

        /**
         * Updates the placeholder, which is shown when no [CartesianChartModel] is available.
         */
        public fun setPlaceholder(
            view: View?,
            params: LayoutParams? = null,
        ) {
            if (view === placeholder) return
            removeAllViews()
            if (view != null) addView(view, params)
            placeholder = view
            updatePlaceholderVisibility()
        }

        protected fun updatePlaceholderVisibility() {
            placeholder?.isVisible = shouldShowPlaceholder()
        }

        protected abstract fun shouldShowPlaceholder(): Boolean

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val width = widthMeasureSpec.specSize.coerceAtLeast(suggestedMinimumWidth)
            val defaultHeight = getChartDesiredHeight(widthMeasureSpec, heightMeasureSpec) + verticalPadding

            val height =
                when (MeasureSpec.getMode(heightMeasureSpec)) {
                    MeasureSpec.EXACTLY -> heightMeasureSpec.specSize
                    MeasureSpec.AT_MOST -> defaultHeight.coerceAtMost(heightMeasureSpec.specSize)
                    else -> defaultHeight
                }

            super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
            )
            contentBounds.set(
                left = paddingLeft,
                top = paddingTop,
                right = width - paddingRight,
                bottom = height - paddingBottom,
            )
        }

        protected fun startAnimation(transformModel: suspend (key: Any, fraction: Float) -> Unit) {
            if (model != null || runInitialAnimation) {
                handler?.post {
                    isAnimationRunning = true
                    animator.start { fraction ->
                        when {
                            !isAnimationRunning -> return@start
                            !isAnimationFrameGenerationRunning -> {
                                isAnimationFrameGenerationRunning = true
                                animationFrameJob =
                                    coroutineScope?.launch(dispatcher) {
                                        transformModel(this@BaseChartView, fraction)
                                        isAnimationFrameGenerationRunning = false
                                    }
                            }

                            fraction == 1f -> {
                                finalAnimationFrameJob =
                                    coroutineScope?.launch(dispatcher) {
                                        animationFrameJob?.cancelAndJoin()
                                        transformModel(this@BaseChartView, fraction)
                                        isAnimationFrameGenerationRunning = false
                                    }
                            }
                        }
                    }
                }
            } else {
                finalAnimationFrameJob =
                    coroutineScope?.launch(dispatcher) {
                        transformModel(this@BaseChartView, Animation.range.endInclusive)
                    }
            }
        }

        protected abstract fun getChartDesiredHeight(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ): Int

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
            // In this case, we can ignore this callback, as the layout direction will be determined when the
            // MeasureContext instance is created.
            measureContext?.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
        }
    }
