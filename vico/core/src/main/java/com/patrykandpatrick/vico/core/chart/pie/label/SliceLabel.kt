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

package com.patrykandpatrick.vico.core.chart.pie.label

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.layout.PieLayoutHelper

/**
 * TODO
 */
public abstract class SliceLabel {

    /**
     * TODO
     */
    @LongParameterListDrawFunction
    public abstract fun drawLabel(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        slicePath: Path,
        label: CharSequence,
    )

    /**
     * TODO
     */
    @LongParameterListDrawFunction
    public open fun getInsets(
        context: DrawContext,
        contentBounds: RectF,
        oval: RectF,
        angle: Float,
        label: CharSequence,
        outInsets: Insets,
    ): Unit = Unit

    public companion object
}
