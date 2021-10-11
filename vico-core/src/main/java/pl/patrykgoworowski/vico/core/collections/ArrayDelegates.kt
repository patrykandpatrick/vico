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

package pl.patrykgoworowski.vico.core.collections

import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <S, T : AxisRenderer<S>?> cacheInList(): ReadWriteProperty<AxisManager, T?> =
    object : ReadWriteProperty<AxisManager, T?> {
        var field: T? = null

        override fun getValue(thisRef: AxisManager, property: KProperty<*>): T? = field

        override fun setValue(thisRef: AxisManager, property: KProperty<*>, value: T?) {
            if (field == value) return
            field?.let(thisRef.axisCache::remove)
            field = value
            value?.let(thisRef.axisCache::add)
        }
    }
