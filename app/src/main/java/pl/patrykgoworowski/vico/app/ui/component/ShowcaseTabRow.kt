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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import pl.patrykgoworowski.vico.app.extension.material3pagerTabIndicatorOffset
import pl.patrykgoworowski.vico.app.extension.surfaceColorAtElevation

@Composable
@OptIn(ExperimentalPagerApi::class)
internal fun ShowcaseTabRow(
    pagerState: PagerState,
    showcasePages: List<ShowcasePage>,
) {
    val coroutineScope = rememberCoroutineScope()
    val indicatorShape = RoundedCornerShape(
        topStartPercent = 100,
        topEndPercent = 100,
    )
    val scrollToPage = { index: Int ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(page = index)
        }
    }
    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 2.dp)

    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = surfaceColor)
                .statusBarsPadding(),
        )
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation = 2.dp),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .material3pagerTabIndicatorOffset(
                            pagerState = pagerState,
                            tabPositions = tabPositions,
                        )
                        .padding(horizontal = 20.dp)
                        .clip(shape = indicatorShape),
                )
            }
        ) {
            showcasePages.mapIndexed { index, page ->
                Tab(
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    selected = pagerState.currentPage == index,
                    onClick = { scrollToPage(index) },
                    text = {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = stringResource(id = page.labelRes),
                        )
                    }
                )
            }
        }
    }
}
