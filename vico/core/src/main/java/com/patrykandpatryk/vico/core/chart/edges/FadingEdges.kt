/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.chart.edges

import android.animation.TimeInterpolator
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.view.animation.AccelerateDecelerateInterpolator
import com.patrykandpatryk.vico.core.FULL_FADE_SCROLL_THRESHOLD_DP
import com.patrykandpatryk.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatryk.vico.core.extension.copyColor

private const val FULL_ALPHA = 0xFF
private const val FULL_FADE: Int = 0xFF000000.toInt()
private const val NO_FADE: Int = 0x00000000

/**
 * [FadingEdges] applies a horizontal fade for scrollable content inside of a chart.
 * A faded edge indicates a possibility to scroll towards the edge to reveal more content.
 *
 * @param startFadingEdgeLengthDp the length in dp unit of a start edge.
 * @param endFadingEdgeLengthDp the length in dp unit of an end edge.
 * @param fullFadeThresholdDp the amount of scroll in dp unit needed to make the fade fully visible.
 * @param fadeInterpolator interpolates a fade transition of fading edges.
 */
public open class FadingEdges(
    public var startFadingEdgeLengthDp: Float = 0f,
    public var endFadingEdgeLengthDp: Float = startFadingEdgeLengthDp,
    public var fullFadeThresholdDp: Float = FULL_FADE_SCROLL_THRESHOLD_DP,
    public var fadeInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
) {

    private val paint: Paint = Paint()

    private val rect: RectF = RectF()

    /**
     * Creates [FadingEdges] with horizontal edges in equal length.
     *
     * @param fadingEdgesLengthDp the length in dp unit of horizontal edges.
     * @param fullFadeThresholdDp the amount of scroll in dp unit needed to make the fade fully visible.
     * @param fadeInterpolator interpolates a fade transition of fading edges.
     */
    public constructor(
        fadingEdgesLengthDp: Float = 0f,
        fullFadeThresholdDp: Float = FULL_FADE_SCROLL_THRESHOLD_DP,
        fadeInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    ) : this(
        startFadingEdgeLengthDp = fadingEdgesLengthDp,
        endFadingEdgeLengthDp = fadingEdgesLengthDp,
        fullFadeThresholdDp = fullFadeThresholdDp,
        fadeInterpolator = fadeInterpolator,
    )

    init {
        if (startFadingEdgeLengthDp < 0) error("startFadingEdgeLengthDp cannot be smaller than 0.")
        if (endFadingEdgeLengthDp < 0) error("endFadingEdgeLengthDp cannot be smaller than 0.")

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    /**
     * Applies fading edges inside of given [bounds] accordingly to a scroll state.
     *
     * @param context the drawing context that holds the information necessary to draw the fading edges.
     * @param bounds within which the fading edges will be drawn.
     */
    public fun applyFadingEdges(
        context: ChartDrawContext,
        bounds: RectF,
    ): Unit = with(context) {

        val maxScroll = getMaxScrollDistance()
        var fadeAlphaFraction: Float

        if (isHorizontalScrollEnabled && startFadingEdgeLengthDp > 0f && horizontalScroll > 0f) {

            fadeAlphaFraction = (horizontalScroll / fullFadeThresholdDp.pixels).coerceAtMost(1f)

            drawFadingEdge(
                left = bounds.left,
                top = bounds.top,
                right = bounds.left + startFadingEdgeLengthDp.pixels,
                bottom = bounds.bottom,
                direction = -1,
                alpha = (fadeInterpolator.getInterpolation(fadeAlphaFraction) * FULL_ALPHA).toInt(),
            )
        }

        if (isHorizontalScrollEnabled && endFadingEdgeLengthDp > 0f && horizontalScroll < maxScroll) {

            fadeAlphaFraction = ((maxScroll - horizontalScroll) / fullFadeThresholdDp.pixels).coerceAtMost(1f)

            drawFadingEdge(
                left = bounds.right - endFadingEdgeLengthDp.pixels,
                top = bounds.top,
                right = bounds.right,
                bottom = bounds.bottom,
                direction = 1,
                alpha = (fadeInterpolator.getInterpolation(fadeAlphaFraction) * FULL_ALPHA).toInt(),
            )
        }
    }

    @LongParameterListDrawFunction
    private fun ChartDrawContext.drawFadingEdge(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        direction: Int,
        alpha: Int,
    ) {
        rect.set(left, top, right, bottom)

        val faded = FULL_FADE.copyColor(alpha = alpha)

        paint.shader = LinearGradient(
            rect.left,
            0f,
            rect.right,
            0f,
            if (direction < 0) faded else NO_FADE,
            if (direction < 0) NO_FADE else faded,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(rect, paint)
    }
}
