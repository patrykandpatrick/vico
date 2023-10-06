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

package com.patrykandpatrick.vico.compose.component.shape.shader

import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders

/**
 * Creates a [ComposeShader] out of two [DynamicShader]s by using a [BlendMode].
 */
@Deprecated(
    message = "This function has been moved to the `core` module.",
    replaceWith = ReplaceWith(
        expression = "DynamicShaders.composeShader(first, second, mode)",
        imports = ["com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders"],
    ),
)
@RequiresApi(Build.VERSION_CODES.Q)
public fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: BlendMode,
): DynamicShader = DynamicShaders.composeShader(first, second, mode)

/**
 * Creates a [ComposeShader] out of two [DynamicShader]s by using a [PorterDuff.Mode].
 */
@Deprecated(
    message = "This function has been moved to the `core` module.",
    replaceWith = ReplaceWith(
        expression = "DynamicShaders.composeShader(first, second, mode)",
        imports = ["com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders"],
    ),
)
public fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: PorterDuff.Mode,
): DynamicShader = DynamicShaders.composeShader(first, second, mode)
