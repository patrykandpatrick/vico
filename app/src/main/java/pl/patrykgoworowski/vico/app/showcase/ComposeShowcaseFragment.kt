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

package pl.patrykgoworowski.vico.app.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pl.patrykgoworowski.vico.app.component.compose.ScrollableColumn
import pl.patrykgoworowski.vico.app.component.compose.markerComponent
import pl.patrykgoworowski.vico.app.ui.*
import pl.patrykgoworowski.vico.compose.component.dimension.dimensionsOf
import pl.patrykgoworowski.vico.compose.component.rectComponent
import pl.patrykgoworowski.vico.compose.component.shape.shader.componentShader
import pl.patrykgoworowski.vico.compose.component.shape.shader.horizontalGradient
import pl.patrykgoworowski.vico.compose.component.shapeComponent
import pl.patrykgoworowski.vico.compose.dataset.bar.DataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.columnDataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.lineDataSet
import pl.patrykgoworowski.vico.core.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.core.axis.vertical.startAxis
import pl.patrykgoworowski.vico.core.dataset.bar.MergeMode
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.path.PillShape

class ComposeShowcaseFragment : Fragment() {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    private val chartModifier
        get() = Modifier
            //.fillMaxWidth()
            .wrapContentWidth()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView

        composeView.setContent {
            MainTheme {

                ScrollableColumn {

                    Spacer(modifier = Modifier.height(24.dp))

                    DataSet(
                        modifier = chartModifier,
                        dataSet = columnDataSet(
                            column = rectComponent(
                                color = flickrPink,
                                thickness = 16.dp,
                                shape = CutCornerShape(topStart = 8f.dp),
                                dynamicShader = horizontalGradient(
                                    arrayOf(
                                        flickrPink,
                                        trypanPurple
                                    )
                                ),
                            ),
                        ),
                        entryCollection = viewModel.entries,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(),
                        marker = markerComponent(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val composedDataSet = ComposedDataSet(
                        listOf(
                            columnDataSet(
                                columns = listOf(
                                    rectComponent(
                                        color = trypanPurple,
                                        thickness = 16.dp,
                                        shape = CutCornerShape(topStart = 8.dp),
                                    ),
                                    rectComponent(
                                        color = byzantine,
                                        thickness = 12.dp,
                                        shape = PillShape,
                                    ),
                                    rectComponent(
                                        color = purple,
                                        thickness = 16.dp,
                                        shape = CutCornerShape(topEnd = 8.dp),
                                    ),
                                ),
                            ), lineDataSet(
                                pointSize = 62.dp,
                                lineColor = flickrPink,
                                spacing = 8.dp
                            )
                        )
                    )

                    DataSet(
                        modifier = chartModifier,
                        dataSet = composedDataSet,
                        entryCollection = viewModel.composedEntries,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(),
                        marker = markerComponent(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DataSet(
                        modifier = chartModifier,
                        dataSet = lineDataSet(
                            pointSize = 10.dp,
                            lineColor = flickrPink,
                            lineBackgroundShader = componentShader(
                                component = shapeComponent(
                                    shape = PillShape,
                                    color = flickrPink,
                                    margins = dimensionsOf(all = 0.5f.dp)
                                ),
                                componentSize = 4.dp,
                            ),
                        ),
                        entryCollection = viewModel.entries,
                        marker = markerComponent(),
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DataSet(
                        modifier = chartModifier,
                        dataSet = columnDataSet(
                            columns = listOf(
                                rectComponent(
                                    color = flickrPink,
                                    shape = CutCornerShape(topStart = 8.dp)
                                ),
                                rectComponent(color = byzantine, thickness = 24.dp),
                                rectComponent(
                                    color = trypanPurple,
                                    shape = CutCornerShape(topEnd = 8.dp)
                                ),
                            ),
                            innerSpacing = 4.dp,
                            spacing = 24.dp,
                            mergeMode = MergeMode.Grouped,
                        ),
                        entryCollection = viewModel.multiEntries,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(),
                        marker = markerComponent(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DataSet(
                        modifier = chartModifier,
                        dataSet = columnDataSet(
                            columns = listOf(
                                rectComponent(
                                    color = flickrPink,
                                    shape = CutCornerShape(bottomEnd = 8.dp)
                                ),
                                rectComponent(color = byzantine),
                                rectComponent(
                                    color = trypanPurple,
                                    shape = CutCornerShape(topStart = 8.dp)
                                ),
                            ),
                            innerSpacing = 4.dp,
                            spacing = 24.dp,
                            mergeMode = MergeMode.Stack,
                        ),
                        entryCollection = viewModel.multiEntries,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(),
                        marker = markerComponent(),
                    )
                }
            }
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.onSurface,
        modifier = modifier
            .padding(Dp(16f))
    )
}
