package com.patrykandpatrick.vico.multiplatform.common

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

internal actual val runBlocking: ((CoroutineContext, suspend CoroutineScope.() -> Unit) -> Unit)?
  get() = null
