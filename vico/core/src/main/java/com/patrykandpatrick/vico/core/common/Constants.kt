/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.common

import androidx.annotation.RestrictTo

internal const val ERR_REPEATING_COLLECTION_EMPTY =
  "Cannot get repeated item from empty collection."

internal const val ELLIPSIS = "…"

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public const val NEW_PRODUCER_ERROR_MESSAGE: String =
  "A new `CartesianChartModelProducer` was provided. Run data updates via `tryRunTransaction` or " +
    "`runTransaction`, not by creating new `CartesianChartModelProducer`s."
