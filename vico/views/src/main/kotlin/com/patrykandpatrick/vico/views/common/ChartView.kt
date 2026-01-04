/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.patrykandpatrick.vico.views.cartesian.CartesianChart
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.data.MutableExtraStore
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.*

/** Displays a [CartesianChart]. */
public abstract class ChartView<M>
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  FrameLayout(context, attrs, defStyleAttr) {

  private val _canvasSize = MutableSize()

  protected val canvasSize: Size = _canvasSize

  private val _measuringContext =
    MutableMeasuringContext(
      canvasSize,
      context.density,
      ExtraStore.Empty,
      context.isLtr,
      context::spToPx,
    )

  protected open val measuringContext: MeasuringContext = _measuringContext

  /** Houses the chart data. */
  public abstract var model: M?

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

  private val _offset: MutableOffset = MutableOffset()

  protected val offset: Offset = _offset

  private val motionEventMatrix: Matrix = Matrix()

  private var shouldAcceptMotionEvents = false

  /** Whether to run an initial animation when the [ChartView] is created. */
  public var animateIn: Boolean = true

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    coroutineScope = CoroutineScope(EmptyCoroutineContext)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    coroutineScope?.cancel()
    coroutineScope = null
    animator.cancel()
    isAnimationRunning = false
  }

  override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
    check(childCount == 0) { "Only one placeholder can be added." }
    super.addView(child, index, params)
    placeholder = child
    updatePlaceholderVisibility()
  }

  override fun onViewRemoved(child: View?) {
    super.onViewRemoved(child)
    placeholder = null
  }

  /** Updates the placeholder, which is shown when no chart data is available. */
  public fun setPlaceholder(view: View?, params: LayoutParams? = null) {
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

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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

    _canvasSize.width = (width - paddingLeft - paddingRight).toFloat()
    _canvasSize.height = (height - paddingTop - paddingBottom).toFloat()
    _offset.x = paddingLeft.toFloat()
    _offset.y = paddingTop.toFloat()
    motionEventMatrix.setTranslate(-offset.x, -offset.y)
  }

  protected fun startAnimation(transformModel: suspend (key: Any, fraction: Float) -> Unit) {
    if (model != null || animateIn) {
      handler?.post {
        isAnimationRunning = true
        animator.start { fraction ->
          when {
            !isAnimationRunning -> return@start
            !isAnimationFrameGenerationRunning -> {
              isAnimationFrameGenerationRunning = true
              animationFrameJob =
                coroutineScope?.launch {
                  transformModel(this@ChartView, fraction)
                  isAnimationFrameGenerationRunning = false
                }
            }
            fraction == 1f -> {
              finalAnimationFrameJob =
                coroutineScope?.launch(Dispatchers.Default) {
                  animationFrameJob?.cancelAndJoin()
                  transformModel(this@ChartView, fraction)
                  isAnimationFrameGenerationRunning = false
                }
            }
          }
        }
      }
    } else {
      finalAnimationFrameJob =
        coroutineScope?.launch { transformModel(this@ChartView, Animation.range.endInclusive) }
    }
  }

  protected abstract fun getChartDesiredHeight(widthMeasureSpec: Int, heightMeasureSpec: Int): Int

  /** Sets the duration (in milliseconds) of difference animations. */
  public fun setAnimationDuration(durationMillis: Long) {
    animator.duration = durationMillis
  }

  /** Sets the [Interpolator] for difference animations. */
  public fun setAnimationInterpolator(interpolator: Interpolator) {
    animator.interpolator = interpolator
  }

  override fun onRtlPropertiesChanged(layoutDirection: Int) {
    /* This function may be called by the `View` constructor, in which case `measuringContext` won’t
    have been initialized yet. Such calls can be ignored, as `Context.isLtr` will be read at
    `MutableMeasuringContext` instantiation. */
    @Suppress("UNNECESSARY_SAFE_CALL")
    _measuringContext?.isLtr = layoutDirection == LAYOUT_DIRECTION_LTR
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val superHandled = super.onTouchEvent(event)
    event.transform(motionEventMatrix)
    return superHandled
  }

  protected fun MotionEvent.shouldAccept(): Boolean {
    if (actionMasked == MotionEvent.ACTION_DOWN) {
      shouldAcceptMotionEvents =
        (0..<pointerCount).all { index ->
          getX(index) in offset.x..offset.x + canvasSize.width &&
            getY(index) in offset.y..offset.y + canvasSize.height
        }
    }
    if (!shouldAcceptMotionEvents) return false
    if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
      shouldAcceptMotionEvents = false
    }
    return true
  }
}

private fun ValueAnimator.start(block: (Float) -> Unit) {
  val updateListener = ValueAnimator.AnimatorUpdateListener { block(it.animatedFraction) }
  addUpdateListener(updateListener)
  addListener(
    object : AnimatorListenerAdapter() {
      override fun onAnimationCancel(animation: Animator) {
        removeUpdateListener(updateListener)
        removeListener(this)
      }

      override fun onAnimationEnd(animation: Animator) {
        onAnimationCancel(animation)
      }
    }
  )
  start()
}
