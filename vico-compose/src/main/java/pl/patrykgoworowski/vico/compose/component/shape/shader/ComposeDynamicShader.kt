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

package pl.patrykgoworowski.vico.compose.component.shape.shader

import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader

@RequiresApi(Build.VERSION_CODES.Q)
public fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: BlendMode,
): DynamicShader = DynamicShader { context, left, top, right, bottom ->
    ComposeShader(
        first.provideShader(context, left, top, right, bottom),
        second.provideShader(context, left, top, right, bottom),
        mode
    )
}

public fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: PorterDuff.Mode,
): DynamicShader = DynamicShader { context, left, top, right, bottom ->
    ComposeShader(
        first.provideShader(context, left, top, right, bottom),
        second.provideShader(context, left, top, right, bottom),
        mode
    )
}
