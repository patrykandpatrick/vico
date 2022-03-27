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

package pl.patrykgoworowski.vico.sample.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.sample.ShowcaseViewModel
import pl.patrykgoworowski.vico.compose.m3.style.m3ChartStyle
import pl.patrykgoworowski.vico.compose.style.ProvideChartStyle

@Composable
@OptIn(
    ExperimentalPagerApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)
internal fun Showcase() {
    val showcaseViewModel = viewModel<ShowcaseViewModel>()
    val pages = ShowcasePage.values().toList()
    val pagerState = rememberPagerState(initialPage = 0)

    ProvideChartStyle(chartStyle = m3ChartStyle()) {
        Column {
            ShowcaseTabRow(
                pagerState = pagerState,
                showcasePages = pages,
            )
            HorizontalPager(
                state = pagerState,
                count = pages.size,
            ) { index ->
                pages[index].content(showcaseViewModel)
            }
        }
    }
}

internal enum class ShowcasePage(
    @StringRes val labelRes: Int,
    val content: @Composable (ShowcaseViewModel) -> Unit,
) {
    ComposeShowcasePage(
        labelRes = R.string.showcase_compose_title,
        content = { ComposeShowcase(showcaseViewModel = it) },
    ),
    ViewShowcasePage(
        labelRes = R.string.showcase_view_title,
        content = { ViewShowcase(showcaseViewModel = it) },
    ),
}
