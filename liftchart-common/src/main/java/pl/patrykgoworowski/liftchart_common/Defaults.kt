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

package pl.patrykgoworowski.liftchart_common

import android.graphics.Color
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.extension.sp

val DEF_LABEL_COMPONENT: TextComponent
    get() = TextComponent()
        .apply {
            setPadding(start = 4f.dp, top = 2f.dp, end = 4f.dp, bottom = 2f.dp)
        }

val DEF_AXIS_COMPONENT: LineComponent
    get() = LineComponent(Color.BLUE, 2f.dp)

val DEF_TICK_COMPONENT: TickComponent
    get() = TickComponent(Color.BLUE, 2f.dp)

val DEF_GUIDELINE_COMPONENT: LineComponent
    get() = LineComponent(Color.LTGRAY, 1f.dp)

val DEF_LABEL_LINE_COUNT = 1
val DEF_LABEL_COUNT = 99
val DEF_LABEL_SIZE = 12f.sp
val DEF_LABEL_SPACING = 16f.dp

val DEF_MARKER_TICK_SIZE = 6f.dp

val MAX_ZOOM = 10f
val MIN_ZOOM = 0.1f

public val DEF_SHADOW_COLOR: Int = 0x8A000000.toInt()