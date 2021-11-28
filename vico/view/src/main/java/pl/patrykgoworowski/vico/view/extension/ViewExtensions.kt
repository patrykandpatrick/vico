/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.view.extension

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import pl.patrykgoworowski.vico.core.model.Point
import kotlin.math.min

internal fun View.measureDimension(desiredSize: Int, measureSpec: Int): Int {
    val specMode = View.MeasureSpec.getMode(measureSpec)
    val specSize = View.MeasureSpec.getSize(measureSpec)
    return when (specMode) {
        View.MeasureSpec.EXACTLY -> specSize
        View.MeasureSpec.AT_MOST -> min(desiredSize, specSize)
        else -> desiredSize
    }
}

internal val Int.specSize: Int
    get() = View.MeasureSpec.getSize(this)

internal val Int.specMode: Int
    get() = View.MeasureSpec.getMode(this)

internal val View.widthIsWrapContent: Boolean
    get() = layoutParams?.width == WRAP_CONTENT

internal val View.parentOrOwnWidth: Int
    get() = (parent as? View)?.width ?: width

internal var View.horizontalPadding: Int
    get() = paddingLeft + paddingRight
    set(value) {
        updatePadding(left = value / 2, right = value / 2)
    }

internal var View.verticalPadding: Int
    get() = paddingTop + paddingBottom
    set(value) {
        updatePadding(top = value / 2, bottom = value / 2)
    }

internal val View.isLtr: Boolean
    get() = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR

internal fun OverScroller.fling(
    startX: Int = 0,
    startY: Int = 0,
    velocityX: Int = 0,
    velocityY: Int = 0,
) {
    fling(
        startX, startY, velocityX, velocityY, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE,
        Int.MAX_VALUE
    )
}

public val MotionEvent.point: Point
    get() = Point(x, y)
