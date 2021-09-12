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

package pl.patrykgoworowski.vico.app

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.extension.enableEdgeToEdge
import pl.patrykgoworowski.vico.app.extension.statusBarInsets
import pl.patrykgoworowski.vico.app.ui.addOnTabSelectedListener
import pl.patrykgoworowski.vico.core.extension.dp
import pl.patrykgoworowski.vico.core.path.Shapes.roundedCornersShape
import pl.patrykgoworowski.vico.core.path.toDrawable
import pl.patrykgoworowski.vico.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val tabIndicator: Drawable by lazy {
        roundedCornersShape(topLeftPercent = 100, topRightPercent = 100)
            .toDrawable(intrinsicHeight = 3.dp.toInt())
    }

    private val navigator = Navigator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) navigator.navigateTo(Navigator.Screen.ComposeShowcase)

        with(binding) {
            tabLayout.apply {
                setOnApplyWindowInsetsListener { view, insets ->
                    view.updatePadding(top = insets.statusBarInsets.top)
                    insets
                }
                setSelectedTabIndicator(tabIndicator)
                addTab(newTab().setText(R.string.showcase_compose_title))
                addTab(newTab().setText(R.string.showcase_view_title))
                addOnTabSelectedListener { tab ->
                    val screen = when (tab.position) {
                        0 -> Navigator.Screen.ComposeShowcase
                        1 -> Navigator.Screen.ViewShowcase
                        else -> throw IndexOutOfBoundsException("Tab index out of bounds.")
                    }
                    navigator.navigateTo(screen)
                }
            }
        }
    }
}
