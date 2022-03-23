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

package pl.patrykgoworowski.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.cornered.Corner
import pl.patrykgoworowski.vico.core.component.shape.cornered.CornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.cornered.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.cornered.RoundedCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.cornered.SharpCornerTreatment
import pl.patrykgoworowski.vico.view.R

private const val ONE_HUNDRED_PERCENT = 100

internal fun TypedArray.getShape(
    context: Context,
): Shape {
    val shape = CorneredShape(
        topLeft = getCorner(
            context = context,
            sizeIndex = R.styleable.Shape_topStartCornerSize,
            treatmentIndex = R.styleable.Shape_topStartCornerTreatment,
        ),
        topRight = getCorner(
            context = context,
            sizeIndex = R.styleable.Shape_topEndCornerSize,
            treatmentIndex = R.styleable.Shape_topEndCornerTreatment,
        ),
        bottomLeft = getCorner(
            context = context,
            sizeIndex = R.styleable.Shape_bottomStartCornerSize,
            treatmentIndex = R.styleable.Shape_bottomStartCornerTreatment,
        ),
        bottomRight = getCorner(
            context = context,
            sizeIndex = R.styleable.Shape_bottomEndCornerSize,
            treatmentIndex = R.styleable.Shape_bottomEndCornerTreatment,
        ),
    )

    val dashLength = getRawDimension(
        context = context,
        index = R.styleable.Shape_dashLength,
        defaultValue = 0f
    )
    val dashGapLength = getRawDimension(
        context = context,
        index = R.styleable.Shape_dashGapLength,
        defaultValue = 0f
    )

    return if (dashLength == 0f) {
        shape
    } else {
        DashedShape(
            shape = shape,
            dashLengthDp = dashLength,
            gapLengthDp = dashGapLength,
        )
    }
}

private fun TypedArray.getCorner(
    context: Context,
    @StyleableRes sizeIndex: Int,
    @StyleableRes treatmentIndex: Int,
    handleNullSizeIndex: Boolean = true
): Corner = when {
    !hasValue(sizeIndex) && handleNullSizeIndex -> {
        getCorner(
            context = context,
            sizeIndex = R.styleable.Shape_cornerSize,
            treatmentIndex = treatmentIndex,
            handleNullSizeIndex = false
        )
    }
    isFraction(sizeIndex) -> {
        val percentage = (getFraction(sizeIndex, defaultValue = 0f) * ONE_HUNDRED_PERCENT).toInt()
        Corner.Relative(
            percentage = percentage,
            cornerTreatment = if (percentage == 0) {
                SharpCornerTreatment
            } else {
                getCornerTreatment(treatmentIndex)
            }
        )
    }
    else -> {
        val sizeDp = getRawDimension(context, sizeIndex, defaultValue = 0f)
        Corner.Absolute(
            sizeDp = sizeDp,
            cornerTreatment = if (sizeDp == 0f) {
                SharpCornerTreatment
            } else {
                getCornerTreatment(treatmentIndex)
            },
        )
    }
}

private fun TypedArray.getCornerTreatment(
    @StyleableRes index: Int,
    defaultValue: Int = -1
): CornerTreatment =
    when (getInt(index, defaultValue)) {
        -1 -> getCornerTreatment(R.styleable.Shape_cornerTreatment, defaultValue = 0)
        0 -> RoundedCornerTreatment
        else -> CutCornerTreatment
    }
