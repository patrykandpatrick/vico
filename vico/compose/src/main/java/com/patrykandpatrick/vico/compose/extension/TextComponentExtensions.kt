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

package com.patrykandpatrick.vico.compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.core.component.text.TextComponent

/**
 * Creates and remembers [TextComponent.MinWidth.TextLength].
 */
@Stable
@Composable
public fun rememberMinWidthOfTextLength(text: String): TextComponent.MinWidth.TextLength =
    remember(text) {
        TextComponent.MinWidth.TextLength(text)
    }

/**
 * Creates and remembers [TextComponent.MinWidth.Fixed].
 */
@Stable
@Composable
public fun rememberFixedMinWidth(value: Dp): TextComponent.MinWidth.Fixed =
    remember(value) {
        TextComponent.MinWidth.Fixed(value.value)
    }
