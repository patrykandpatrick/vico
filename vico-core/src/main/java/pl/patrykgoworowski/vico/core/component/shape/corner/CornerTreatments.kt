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

package pl.patrykgoworowski.vico.core.component.shape.corner

import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.extension.piRad

object SharpCornerTreatment : CornerTreatment {

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) = when (cornerLocation) {
        CornerLocation.TopLeft -> {
            path.lineTo(x1, y2)
        }
        CornerLocation.TopRight -> {
            path.lineTo(x2, y1)
        }
        CornerLocation.BottomRight -> {
            path.lineTo(x1, y2)
        }
        CornerLocation.BottomLeft -> {
            path.lineTo(x2, y1)
        }
    }
}

object CutCornerTreatment : CornerTreatment {

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) {
        path.lineTo(x1, y1)
        path.lineTo(x2, y2)
    }
}

object RoundedCornerTreatment : CornerTreatment {

    private val tempRect = RectF()

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) {
        val startAngle: Float
        when (cornerLocation) {
            CornerLocation.TopLeft -> {
                startAngle = 1f.piRad
                tempRect.set(x1, y2, (x2 * 2) - x1, (y1 * 2) - y2)
            }
            CornerLocation.TopRight -> {
                startAngle = 1.5f.piRad
                tempRect.set((x1 * 2) - x2, y1, x2, (y2 * 2) - y1)
            }
            CornerLocation.BottomRight -> {
                startAngle = 0f
                tempRect.set((x2 * 2) - x1, (y1 * 2) - y2, x1, y2)
            }
            CornerLocation.BottomLeft -> {
                startAngle = 0.5f.piRad
                tempRect.set(x2, (y2 * 2) - y1, (x1 * 2) - x2, y1)
            }
        }
        path.arcTo(tempRect, startAngle, 0.5f.piRad)
    }
}
