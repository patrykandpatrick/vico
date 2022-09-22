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

package com.patrykandpatryk.vico.core.scroll

import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.ifNotNull

// TODO some better name?
public fun interface AutoScroll<in Model : ChartEntryModel> {

    public fun shouldPerformAutoScroll(newModel: Model, oldModel: Model?): Boolean

    public companion object {

        public val Disabled: AutoScroll<ChartEntryModel> = AutoScroll { _, _ -> false }

        public val OnModelSizeIncreased: AutoScroll<ChartEntryModel> = AutoScroll { n, o ->
            if (o != null) {
                val new = n.entries
                val old = o.entries
                new.size > old.size ||
                    ifNotNull(
                        t1 = new.maxOfOrNull { entries -> entries.size },
                        t2 = old.maxOfOrNull { entries -> entries.size },
                    ) { t1, t2 -> t1 > t2 } == true
            } else false
        }
    }
}
