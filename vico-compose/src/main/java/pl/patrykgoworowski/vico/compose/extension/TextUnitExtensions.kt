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

package pl.patrykgoworowski.vico.compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import pl.patrykgoworowski.vico.core.DEF_LABEL_SIZE

fun TextUnit.pixelSize(density: Density) =
    when (type) {
        TextUnitType.Sp -> with(density) { toPx() }
        TextUnitType.Em -> value
        else -> DEF_LABEL_SIZE
    }

@Composable
fun TextUnit.pixelSize() = pixelSize(LocalDensity.current)