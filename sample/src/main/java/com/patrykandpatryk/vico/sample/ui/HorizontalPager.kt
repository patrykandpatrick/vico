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

package com.patrykandpatryk.vico.sample.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterialApi::class)
internal fun HorizontalPager(
    itemCount: Int,
    state: SwipeableState<Int>,
    above: (@Composable () -> Unit)? = null,
    below: (@Composable () -> Unit)? = null,
    getPage: @Composable (Int) -> Unit,
) {
    val density = LocalDensity.current
    val indices = 0 until itemCount
    BoxWithConstraints {
        val itemWidth = maxWidth
        val itemWidthPx = with(density) { itemWidth.toPx() }
        val fractionalState = state.offset.value / itemWidthPx
        val anchors = indices.associateBy { itemWidthPx * it }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .swipeable(
                    state = state,
                    anchors = anchors,
                    orientation = Orientation.Horizontal,
                    reverseDirection = true,
                ),
        ) {
            above?.invoke()
            Box(modifier = Modifier.fillMaxWidth()) {
                indices.map { index ->
                    val delta = -fractionalState + index
                    val xOffset = (delta * itemWidth.value).dp
                    if (delta in -1f..1f) {
                        key(index) {
                            Box(
                                modifier = Modifier
                                    .offset(x = xOffset)
                                    .fillMaxWidth(),
                            ) { getPage(index) }
                        }
                    }
                }
            }
            below?.invoke()
        }
    }
}
