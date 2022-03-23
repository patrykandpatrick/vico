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

package pl.patrykgoworowski.vico.core.throwable

/**
 * The [Exception] thrown when a function receives an unexpected [pl.patrykgoworowski.vico.core.axis.AxisPosition]
 * subclass.
 */
public class UnknownAxisPositionException(
    receivedClass: Class<*>,
) : IllegalArgumentException("Got unknown AxisPosition class ${receivedClass.name}")
