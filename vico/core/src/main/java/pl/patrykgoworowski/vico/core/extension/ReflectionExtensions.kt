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

package pl.patrykgoworowski.vico.core.extension

public inline fun <reified T, V> T.setFieldValue(fieldName: String, value: V) {
    val field = T::class.java.getDeclaredField(fieldName)
    val wasAccessible = field.isAccessible
    field.isAccessible = true
    field.set(this, value)
    field.isAccessible = wasAccessible
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T, V> T.getFieldValue(fieldName: String): V {
    val field = T::class.java.getDeclaredField(fieldName)
    val wasAccessible = field.isAccessible
    field.isAccessible = true
    val value = field.get(this) as V
    field.isAccessible = wasAccessible
    return value
}
