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

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.ShowcaseViewModel
import pl.patrykgoworowski.vico.app.extension.surfaceColorAtElevation
import pl.patrykgoworowski.vico.compose.style.LocalChartStyle

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

@Composable
@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
internal fun Showcase() {
    val showcaseViewModel = viewModel<ShowcaseViewModel>()
    val density = LocalDensity.current
    val windowInsets = LocalWindowInsets.current
    val navigationBarHeight = windowInsets.navigationBars.bottom
    val pages = ShowcasePage.values().toList()
    val pagerState = rememberPagerState(initialPage = 0)
    val chartStyleOverrideManager = showcaseViewModel.chartStyleOverrideManager
    var firstSheetItemHeight by remember { mutableStateOf(value = 0) }
    val sheetPeekHeight = with(density) { (firstSheetItemHeight + navigationBarHeight).toDp() }
    val elevatedSurfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 2.dp)

    ProvideOverriddenChartStyle(chartStyleOverrides = chartStyleOverrideManager.chartStyleOverrides) {
        Box(modifier = Modifier.background(color = elevatedSurfaceColor)) {
            BottomSheetScaffold(
                modifier = Modifier.statusBarsPadding(),
                backgroundColor = MaterialTheme.colorScheme.background,
                sheetBackgroundColor = elevatedSurfaceColor,
                sheetElevation = 0.dp,
                sheetPeekHeight = sheetPeekHeight,
                sheetContent = {
                    ConfigurationSheet(
                        chartStyleOverrideManager = chartStyleOverrideManager,
                        chartStyle = LocalChartStyle.current,
                    ) { firstSheetItemHeight = it }
                },
            ) { innerPadding ->
                Column(modifier = Modifier.padding(paddingValues = innerPadding)) {
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
    }
}
