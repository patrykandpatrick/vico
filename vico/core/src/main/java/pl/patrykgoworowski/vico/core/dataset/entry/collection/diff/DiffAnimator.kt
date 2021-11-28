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

package pl.patrykgoworowski.vico.core.dataset.entry.collection.diff

import android.animation.TimeInterpolator

public interface DiffAnimator {

    public val currentProgress: Float

    public var animationInterpolator: TimeInterpolator

    public var animationDuration: Long

    public fun start(onProgress: (progress: Float) -> Unit)

    public fun cancel()
}
