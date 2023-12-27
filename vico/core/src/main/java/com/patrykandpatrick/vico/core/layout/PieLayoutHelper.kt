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

package com.patrykandpatrick.vico.core.layout

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.extension.updateBounds

/**
 * A layout helper class used to measure and adjust the bounds of a label placed inside of a slice.
 */
public open class PieLayoutHelper {
    protected val textBoundPath: Path = Path()

    protected val textIntersectPathTop: Path = Path()

    protected val textIntersectPathBottom: Path = Path()

    protected val textBoundsTop: RectF = RectF()

    protected val textBoundsBottom: RectF = RectF()

    /**
     * Adjusts the bounds of the text to fit inside of the slice.
     *
     * @param textBounds the bounds of the text that will be adjusted.
     * @param slicePath the path of the slice used to adjust the [textBounds].
     */
    public open fun adjustTextBounds(
        textBounds: RectF,
        slicePath: Path,
    ) {
        textIntersectPathTop.rewind()
        textIntersectPathTop.moveTo(textBounds.left, textBounds.top)
        textIntersectPathTop.lineTo(textBounds.right, textBounds.top)
        textIntersectPathTop.lineTo(textBounds.right, textBounds.top + 1)
        textIntersectPathTop.lineTo(textBounds.left, textBounds.top + 1)
        textIntersectPathTop.close()

        textIntersectPathBottom.rewind()
        textIntersectPathBottom.moveTo(textBounds.left, textBounds.bottom)
        textIntersectPathBottom.lineTo(textBounds.right, textBounds.bottom)
        textIntersectPathBottom.lineTo(textBounds.right, textBounds.bottom - 1)
        textIntersectPathBottom.lineTo(textBounds.left, textBounds.bottom - 1)
        textIntersectPathBottom.close()

        textIntersectPathTop.op(slicePath, Path.Op.INTERSECT)
        textIntersectPathBottom.op(slicePath, Path.Op.INTERSECT)

        textIntersectPathTop.computeBounds(textBounds, true)

        val left = textBounds.left
        val right = textBounds.right

        textIntersectPathBottom.computeBounds(textBounds, true)

        textBounds.updateBounds(
            left = left.coerceAtLeast(textBounds.left),
            right = right.coerceAtMost(textBounds.right),
        )
    }
}
