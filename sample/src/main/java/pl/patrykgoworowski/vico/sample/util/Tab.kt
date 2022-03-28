/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package pl.patrykgoworowski.vico.sample.util

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.sample.fragment.ComposeShowcaseFragment
import pl.patrykgoworowski.vico.sample.fragment.ViewShowcaseFragment

internal enum class Tab(
    @StringRes val labelResourceId: Int,
    val createFragment: () -> Fragment,
) {
    ComposeShowcase(
        labelResourceId = R.string.showcase_compose_title,
        createFragment = ::ComposeShowcaseFragment,
    ),
    ViewShowcase(
        labelResourceId = R.string.showcase_view_title,
        createFragment = ::ViewShowcaseFragment,
    )
}
