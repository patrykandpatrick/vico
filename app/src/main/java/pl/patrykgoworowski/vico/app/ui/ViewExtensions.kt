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

package pl.patrykgoworowski.vico.app.ui

import com.google.android.material.tabs.TabLayout

inline fun TabLayout.addOnTabSelectedListener(
    crossinline onTabSelected: (TabLayout.Tab) -> Unit,
) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onTabSelected(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    })
}
