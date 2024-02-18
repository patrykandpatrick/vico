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

package com.patrykandpatrick.vico.core.common.shader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import com.patrykandpatrick.vico.core.common.DrawContext

/**
 * An object that holds simple, anonymous implementations of [DynamicShader].
 */
public object DynamicShaders {
    /**
     * Creates a [DynamicShader] out of the given [bitmap].
     */
    public fun fromBitmap(
        bitmap: Bitmap,
        tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
        tileYMode: Shader.TileMode = tileXMode,
    ): DynamicShader =
        object : CacheableDynamicShader() {
            override fun createShader(
                context: DrawContext,
                left: Float,
                top: Float,
                right: Float,
                bottom: Float,
            ): Shader = BitmapShader(bitmap, tileXMode, tileYMode)
        }

    /**
     * Creates a [ComposeShader] out of two [DynamicShader]s, combining [first] and [second] via [mode].
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    public fun composeShader(
        first: DynamicShader,
        second: DynamicShader,
        mode: BlendMode,
    ): DynamicShader =
        DynamicShader { context, left, top, right, bottom ->
            ComposeShader(
                first.provideShader(context, left, top, right, bottom),
                second.provideShader(context, left, top, right, bottom),
                mode,
            )
        }

    /**
     * Creates a [ComposeShader] out of two [DynamicShader]s, combining [first] and [second] via [mode].
     */
    public fun composeShader(
        first: DynamicShader,
        second: DynamicShader,
        mode: PorterDuff.Mode,
    ): DynamicShader =
        DynamicShader { context, left, top, right, bottom ->
            ComposeShader(
                first.provideShader(context, left, top, right, bottom),
                second.provideShader(context, left, top, right, bottom),
                mode,
            )
        }
}
