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

package com.patrykandpatrick.vico.views.cartesian

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.RectF
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.getDelta
import com.patrykandpatrick.vico.core.cartesian.getMaxScrollDistance
import com.patrykandpatrick.vico.core.common.Animation
import com.patrykandpatrick.vico.core.common.rangeWith

/**
 * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and
 * programmatic scrolling.
 *
 * @property scrollEnabled whether scroll is enabled.
 * @property initialScroll represents the initial scroll value.
 * @property autoScroll represents the scroll value or delta for automatic scrolling.
 * @property autoScrollCondition defines when an automatic scroll should be performed.
 * @property autoScrollInterpolator the [TimeInterpolator] for automatic scrolling.
 * @property autoScrollDuration the animation duration for automatic scrolling.
 */
public class ScrollHandler(
  internal val scrollEnabled: Boolean = true,
  private val initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
  private val autoScroll: Scroll = initialScroll,
  private val autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
  private val autoScrollInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
  private val autoScrollDuration: Long = Animation.DIFF_DURATION.toLong(),
) {
  private val scrollListeners = mutableSetOf<Listener>()
  private var initialScrollHandled = false
  private var context: CartesianMeasureContext? = null
  private var horizontalDimensions: HorizontalDimensions? = null
  private var bounds: RectF? = null
  internal var postInvalidate: (() -> Unit)? = null
  internal var postInvalidateOnAnimation: (() -> Unit)? = null

  private val animator =
    ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
      duration = autoScrollDuration
      interpolator = autoScrollInterpolator
    }

  /** The current scroll value (in pixels). */
  public var value: Float = 0f
    private set(newValue) {
      val oldValue = field
      field = newValue.coerceIn(0f.rangeWith(maxValue))
      if (field != oldValue) scrollListeners.forEach { it.onValueChanged(oldValue, field) }
    }

  /** The maximum scroll value (in pixels). */
  public var maxValue: Float = 0f
    private set(newMaxValue) {
      val oldMaxValue = field
      if (newMaxValue == oldMaxValue) return
      field = newMaxValue
      scrollListeners.forEach { it.onMaxValueChanged(oldMaxValue, field) }
      value = value
    }

  internal fun update(
    context: CartesianMeasureContext,
    bounds: RectF,
    horizontalDimensions: HorizontalDimensions,
  ) {
    this.context = context
    this.horizontalDimensions = horizontalDimensions
    this.bounds = bounds
    maxValue = context.getMaxScrollDistance(bounds.width(), horizontalDimensions)
    if (!initialScrollHandled) {
      value = initialScroll.getValue(context, horizontalDimensions, bounds, maxValue)
      initialScrollHandled = true
    }
  }

  private inline fun withUpdated(
    block: (CartesianMeasureContext, HorizontalDimensions, RectF) -> Unit
  ) {
    val context = this.context
    val horizontalDimensions = this.horizontalDimensions
    val bounds = this.bounds
    if (context != null && horizontalDimensions != null && bounds != null) {
      block(context, horizontalDimensions, bounds)
    }
  }

  internal fun canScroll(delta: Float) =
    delta > 0 && value < maxValue || delta == 0f || delta < 0 && value > 0

  internal fun autoScroll(model: CartesianChartModel, oldModel: CartesianChartModel?) {
    if (!autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) return
    animateScroll(autoScroll, autoScrollInterpolator, autoScrollDuration)
  }

  internal fun saveInstanceState(bundle: Bundle) {
    bundle.putFloat(VALUE_KEY, value)
    bundle.putBoolean(INITIAL_SCROLL_HANDLED_KEY, initialScrollHandled)
  }

  internal fun restoreInstanceState(bundle: Bundle) {
    value = bundle.getFloat(VALUE_KEY)
    initialScrollHandled = bundle.getBoolean(INITIAL_SCROLL_HANDLED_KEY)
  }

  internal fun clearUpdated() {
    context = null
    horizontalDimensions = null
    bounds = null
    postInvalidate = null
    postInvalidateOnAnimation = null
  }

  private fun scrollBy(delta: Float): Float {
    val oldValue = value
    value += delta
    postInvalidate?.invoke()
    return value - oldValue
  }

  private fun animateScrollBy(delta: Float, interpolator: TimeInterpolator, duration: Long): Float {
    val oldValue = value
    val limitedDelta = delta.coerceIn((-value).rangeWith(maxValue - value))
    with(animator) {
      cancel()
      removeAllUpdateListeners()
      this.interpolator = interpolator
      this.duration = duration
      addUpdateListener { animator ->
        scrollBy(oldValue + animator.animatedFraction * limitedDelta - value)
        postInvalidateOnAnimation?.invoke()
      }
      start()
    }
    return limitedDelta
  }

  /** Triggers a scroll. */
  public fun scroll(scroll: Scroll) {
    withUpdated { context, horizontalDimensions, bounds ->
      scrollBy(scroll.getDelta(context, horizontalDimensions, bounds, maxValue, value))
    }
  }

  /** Triggers an animated scroll. */
  public fun animateScroll(
    scroll: Scroll,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    duration: Long = Animation.DIFF_DURATION.toLong(),
  ) {
    withUpdated { context, horizontalDimensions, bounds ->
      animateScrollBy(
        scroll.getDelta(context, horizontalDimensions, bounds, maxValue, value),
        interpolator,
        duration,
      )
    }
  }

  /** Adds the provided [Listener]. */
  public fun addListener(listener: Listener): Boolean {
    if (!scrollListeners.add(listener)) return false
    listener.onValueChanged(value, value)
    listener.onMaxValueChanged(maxValue, maxValue)
    return true
  }

  /** Removes the provided [Listener]. */
  public fun removeListener(listener: Listener): Boolean = scrollListeners.remove(listener)

  /** Facilitates listening for scroll events. */
  public interface Listener {
    /** Called when the scroll value changes. */
    public fun onValueChanged(oldValue: Float, newValue: Float) {}

    /** Called when the maximum scroll value changes. */
    public fun onMaxValueChanged(oldMaxValue: Float, newMaxValue: Float) {}
  }

  private companion object {
    const val VALUE_KEY = "value"
    const val INITIAL_SCROLL_HANDLED_KEY = "initialScrollHandled"
  }
}
