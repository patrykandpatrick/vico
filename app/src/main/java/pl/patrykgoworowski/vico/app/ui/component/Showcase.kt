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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.ShowcaseViewModel
import pl.patrykgoworowski.vico.app.ui.theme.MainTheme

private enum class Page(
    @StringRes val labelRes: Int,
    val content: @Composable (ShowcaseViewModel) -> Unit,
) {
    ComposeShowcasePage(
        labelRes = R.string.showcase_compose_title,
        content = { ComposeShowcase(viewModel = it) },
    ),
    ViewShowcasePage(
        labelRes = R.string.showcase_view_title,
        content = { ViewShowcase(showcaseViewModel = it) },
    ),
}

@Composable
@OptIn(ExperimentalPagerApi::class)
fun Showcase(showcaseViewModel: ShowcaseViewModel) {
    val pages = Page.values().toList()
    val pagerState = rememberPagerState(pageCount = pages.size)
    val coroutineScope = rememberCoroutineScope()

    MainTheme {
        Column {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.primary,
                divider = {},
                modifier = Modifier.shadow(elevation = 2.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .pagerTabIndicatorOffset(pagerState, tabPositions)
                            .clip(
                                shape = RoundedCornerShape(
                                    topStartPercent = 100,
                                    topEndPercent = 100
                                )
                            )
                    )
                }
            ) {
                pages.mapIndexed { index, page ->
                    Tab(
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page = index)
                            }
                        },
                        text = {
                            Text(
                                style = MaterialTheme.typography.body1,
                                text = stringResource(id = page.labelRes)
                            )
                        }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                dragEnabled = false
            ) { index ->
                pages[index].content(showcaseViewModel)
            }
        }
    }
}
