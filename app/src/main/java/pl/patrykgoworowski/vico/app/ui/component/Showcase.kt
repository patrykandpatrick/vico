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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.ShowcaseViewModel
import pl.patrykgoworowski.vico.app.ui.MainTheme

@Composable
fun Showcase(showcaseViewModel: ShowcaseViewModel) {
    var selectedTabIndex by remember { mutableStateOf(value = 0) }

    MainTheme {
        Column {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                divider = {},
                modifier = Modifier.shadow(elevation = 2.dp)
            ) {
                for (i in 0..1) {
                    Tab(
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                        selected = selectedTabIndex == i,
                        onClick = { selectedTabIndex = i },
                        text = {
                            Text(
                                style = MaterialTheme.typography.body1,
                                text = stringResource(
                                    id = if (i == 0) {
                                        R.string.showcase_compose_title
                                    } else {
                                        R.string.showcase_view_title
                                    }
                                )
                            )
                        }
                    )
                }
            }
            Crossfade(targetState = selectedTabIndex) {
                if (it == 0) {
                    ComposeShowcase(viewModel = showcaseViewModel)
                } else {
                    ViewShowcase(showcaseViewModel = showcaseViewModel)
                }
            }
        }
    }
}
