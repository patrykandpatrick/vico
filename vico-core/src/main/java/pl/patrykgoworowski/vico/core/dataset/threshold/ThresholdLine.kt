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

package pl.patrykgoworowski.vico.core.dataset.threshold

import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.text.HorizontalPosition
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.component.text.VerticalPosition

data class ThresholdLine(
    val thresholdValue: Float,
    val thresholdLabel: String = thresholdValue.toString(),
    val lineComponent: LineComponent,
    val textComponent: TextComponent,
    val labelHorizontalPosition: LabelHorizontalPosition = LabelHorizontalPosition.Start,
    val labelVerticalPosition: LabelVerticalPosition = LabelVerticalPosition.Bottom,
) {
    enum class LabelHorizontalPosition(val position: HorizontalPosition) {
        Start(HorizontalPosition.Start),
        End(HorizontalPosition.End),
    }

    enum class LabelVerticalPosition(val position: VerticalPosition) {
        Top(VerticalPosition.Bottom),
        Bottom(VerticalPosition.Top),
    }
}
