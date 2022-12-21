/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.sample.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.patrykandpatryk.vico.R

internal enum class Tab(
    @StringRes val labelResourceId: Int,
    @DrawableRes val iconResourceId: Int,
) {
    Compose(
        labelResourceId = R.string.compose,
        iconResourceId = R.drawable.ic_compose,
    ),
    Views(
        labelResourceId = R.string.views,
        iconResourceId = R.drawable.ic_views,
    )
}
