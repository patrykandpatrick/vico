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

import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder

/**
 * A SDK version-dependant compat function for appending text to [SpannableStringBuilder] and setting the span.
 */
public fun SpannableStringBuilder.appendCompat(
    text: CharSequence,
    what: Any,
    flags: Int,
): SpannableStringBuilder =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        append(text, what, flags)
    } else {
        append(text, 0, text.length)
        setSpan(what, length - text.length, length, flags)
        this
    }

/**
 * Converts the receiver [Iterable] to the [Spannable].
 *
 * @param separator separates items.
 * @param prefix the prefix of the generated [Spannable].
 * @param postfix the postfix of the generated [Spannable].
 * @param limit of item count transformed to the [Spannable].
 * @param truncated the truncation added to the [Spannable] when the [limit] is reached.
 * @param transform the lambda function yielding [SpannableStringBuilder] as its receiver. It is called for each item.
 * The caller function is responsible for adding the text to the builder.
 */
public fun <T> Iterable<T>.transformToSpannable(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "…",
    transform: SpannableStringBuilder.(T) -> Unit,
): Spannable {
    val buffer = SpannableStringBuilder()
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            buffer.transform(element)
        } else break
    }
    if (limit in 0 until count) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}
