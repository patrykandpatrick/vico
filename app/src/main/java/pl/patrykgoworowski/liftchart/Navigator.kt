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

package pl.patrykgoworowski.liftchart

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import pl.patrykgoworowski.liftchart.showcase.ComposeShowcaseFragment
import pl.patrykgoworowski.liftchart.showcase.ViewShowcaseFragment

class Navigator(
    private val activity: FragmentActivity,
    @IdRes private val containerId: Int = R.id.fragment_view,
) {

    enum class Screen {
        ComposeShowcase,
        ViewShowcase
    }

    fun navigateTo(screen: Screen) {
        activity.supportFragmentManager
            .beginTransaction()
            .replace(containerId, createFragment(screen))
            .commit()
    }

    private fun createFragment(screen: Screen): Fragment = when (screen) {
        Screen.ComposeShowcase -> ComposeShowcaseFragment()
        Screen.ViewShowcase -> ViewShowcaseFragment()
    }

}