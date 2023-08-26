/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.entry.diff

import com.patrykandpatrick.vico.core.extension.setToAllChildren
import java.util.TreeMap
import kotlin.math.max

public class DefaultDrawingModelInterpolator<T : DrawingInfo> : DrawingModelInterpolator<T> {

    private val transformationMaps = ArrayList<TreeMap<Float, TransformationModel<T>>>()
    private val oldDrawingInfo = ArrayList<ArrayList<T>>()
    private val newDrawingInfo = ArrayList<ArrayList<T>>()

    override fun setItems(old: List<List<T>>, new: List<List<T>>) {
        synchronized(this) {
            oldDrawingInfo.setToAllChildren(old)
            newDrawingInfo.setToAllChildren(new)
            updateTransformationMap()
        }
    }

    override fun setItems(new: List<List<T>>) {
        setItems(newDrawingInfo, new)
    }

    override fun transform(progress: Float): List<List<T>> = synchronized(this) {
        transformationMaps.mapNotNull { map ->
            map
                .mapNotNull { (_, model) -> model.transform(progress) }
                .takeIf { list -> list.isNotEmpty() }
        }
    }

    private fun updateTransformationMap() {
        transformationMaps.clear()
        repeat(max(oldDrawingInfo.size, newDrawingInfo.size)) { i ->
            val map = TreeMap<Float, TransformationModel<T>>()
            oldDrawingInfo
                .getOrNull(i)
                ?.forEach { map[it.entry.x] = TransformationModel(it) }
            newDrawingInfo
                .getOrNull(i)
                ?.forEach { item ->
                    val current = map[item.entry.x]
                    map[item.entry.x] = TransformationModel(current?.old, item)
                }
            transformationMaps.add(map)
        }
    }

    private class TransformationModel<T : DrawingInfo>(val old: T?, val new: T? = null) {
        fun transform(fraction: Float): T? = new?.transform(old, fraction)
    }
}

@Suppress("UNCHECKED_CAST")
public fun <T : DrawingInfo> T.transform(from: T?, fraction: Float): T = unsafeTransform(from, fraction) as T
