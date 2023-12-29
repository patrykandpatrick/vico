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

package com.patrykandpatrick.vico.views.chart.pie

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.draw.drawContext
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.core.model.PieModel
import com.patrykandpatrick.vico.views.chart.BaseChartView
import com.patrykandpatrick.vico.views.extension.isAttachedToWindowCompat
import com.patrykandpatrick.vico.views.extension.specSize
import com.patrykandpatrick.vico.views.theme.PieChartStyleHandler
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A [View] that displays a pie chart.
 */
public open class PieChartView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : BaseChartView(context, attrs, defStyleAttr) {
        private val pieChartStyleHandler: PieChartStyleHandler =
            PieChartStyleHandler(
                context = context,
                attrs = attrs,
            )

        protected val pieChart: PieChart =
            PieChart(
                slices = pieChartStyleHandler.slices,
                spacingDp = pieChartStyleHandler.sliceSpacing,
                outerSize = pieChartStyleHandler.outerSize,
                innerSize = pieChartStyleHandler.innerSize,
                startAngle = pieChartStyleHandler.startAngle,
            )

        /**
         * The [List] of [Slice]s which define the appearance of each slice of the pie chart.
         */
        public var slices: List<Slice>
            get() = pieChart.slices
            set(value) {
                pieChart.slices = value
                invalidate()
            }

        /**
         * The spacing between each slice of the pie chart (in dp).
         */
        public var sliceSpacingDp: Float
            get() = pieChart.spacingDp
            set(value) {
                pieChart.spacingDp = value
                invalidate()
            }

        /**
         * Defines the outer size of the pie chart.
         */
        public var pieOuterSize: Size.OuterSize
            get() = pieChart.outerSize
            set(value) {
                pieChart.outerSize = value
                invalidate()
            }

        /**
         * Defines the inner size of the pie chart.
         */
        public var pieInnerSize: Size.InnerSize
            get() = pieChart.innerSize
            set(value) {
                pieChart.innerSize = value
                invalidate()
            }

        /**
         * Defines the start angle of the pie chart (in degrees).
         */
        public var startAngle: Float
            get() = pieChart.startAngle
            set(value) {
                pieChart.startAngle = value
                invalidate()
            }

        public var model: PieModel? = null
            private set

        public var modelProducer: PieChartModelProducer? = null
            set(value) {
                if (field === value) return
                field?.unregisterFromUpdates(key = this)
                field = value
                if (isAttachedToWindowCompat) registerForUpdates()
            }

        init {
            if (isInEditMode) {
                setModel(model = sampleModel)
            }
        }

        /**
         * Sets the [PieModel] to display.
         */
        public fun setModel(model: PieModel?) {
            this.model = model
            if (isAttachedToWindowCompat) {
                updatePlaceholderVisibility()
                invalidate()
            }
        }

        override fun shouldShowPlaceholder(): Boolean = model == null

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            pieChart.setBounds(
                left = contentBounds.left,
                top = contentBounds.top,
                right = contentBounds.right,
                bottom = measuredHeight - measuredLegendHeight - paddingBottom,
            )
            legend?.setBounds(
                left = contentBounds.left,
                top = pieChart.bounds.bottom,
                right = contentBounds.right,
                bottom = measuredHeight - paddingBottom,
            )
        }

        override fun getChartDesiredHeight(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ): Int = widthMeasureSpec.specSize

        override fun dispatchDraw(canvas: Canvas) {
            super.dispatchDraw(canvas)
            val model = model ?: return

            measureContext.clearExtras()

            val drawContext =
                drawContext(
                    canvas = canvas,
                    density = measureContext.density,
                    isLtr = measureContext.isLtr,
                    elevationOverlayColor = elevationOverlayColor.toLong(),
                    spToPx = context::spToPx,
                )

            pieChart.draw(context = drawContext, model = model)
            legend?.draw(context = drawContext, chartBounds = pieChart.bounds)
        }

        private fun registerForUpdates() {
            coroutineScope?.launch(dispatcher) {
                modelProducer?.registerForUpdates(
                    key = this@PieChartView,
                    cancelAnimation = {
                        handler?.post(animator::cancel)
                        runBlocking {
                            animationFrameJob?.cancelAndJoin()
                            finalAnimationFrameJob?.cancelAndJoin()
                        }
                        isAnimationRunning = false
                        isAnimationFrameGenerationRunning = false
                    },
                    startAnimation = { transformModel ->
                        if (model != null || runInitialAnimation) {
                            handler?.post {
                                isAnimationRunning = true
                                animator.start()
                            }
                        } else {
                            finalAnimationFrameJob =
                                coroutineScope?.launch(dispatcher) {
                                    transformModel(this@PieChartView, Animation.range.endInclusive)
                                }
                        }
                    },
                    prepareForTransformation = { model, extraStore ->
                        pieChart.prepareForTransformation(model, extraStore)
                    },
                    transform = { extraStore, fraction -> pieChart.transform(extraStore, fraction) },
                    extraStore = extraStore,
                ) { model ->
                    post {
                        setModel(model = model)
                        postInvalidateOnAnimation()
                    }
                }
            }
        }

        override fun transformModelForAnimation(fraction: Float) {
            when {
                !isAnimationRunning -> return
                !isAnimationFrameGenerationRunning -> {
                    isAnimationFrameGenerationRunning = true
                    animationFrameJob =
                        coroutineScope?.launch(dispatcher) {
                            modelProducer?.transformModel(this@PieChartView, fraction)
                            isAnimationFrameGenerationRunning = false
                        }
                }

                fraction == 1f -> {
                    finalAnimationFrameJob =
                        coroutineScope?.launch(dispatcher) {
                            animationFrameJob?.cancelAndJoin()
                            modelProducer?.transformModel(this@PieChartView, fraction)
                            isAnimationFrameGenerationRunning = false
                        }
                }
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (modelProducer?.isRegistered(key = this) != true) registerForUpdates()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            modelProducer?.unregisterFromUpdates(key = this)
        }

        public companion object {
            @Suppress("MagicNumber")
            internal val sampleModel =
                PieModel.build(
                    PieModel.Entry(value = 1f, label = "One"),
                    PieModel.Entry(value = 2f, label = "Two"),
                    PieModel.Entry(value = 3f, label = "Three"),
                    PieModel.Entry(value = 1f, label = "Four"),
                )
        }
    }
