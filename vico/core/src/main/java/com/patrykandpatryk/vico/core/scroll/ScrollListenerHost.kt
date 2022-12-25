/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

/**
 * Declares functions for registering and removing [ScrollListener]s.
 */
public interface ScrollListenerHost {

    /**
     * Registers a [ScrollListener].
     */
    public fun registerScrollListener(scrollListener: ScrollListener)

    /**
     * Removes a [ScrollListener].
     */
    public fun removeScrollListener(scrollListener: ScrollListener)
}
