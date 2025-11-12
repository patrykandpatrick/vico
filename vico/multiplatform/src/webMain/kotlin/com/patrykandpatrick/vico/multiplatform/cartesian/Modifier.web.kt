package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.multiplatform.common.getValue
import com.patrykandpatrick.vico.multiplatform.common.rememberWrappedValue
import com.patrykandpatrick.vico.multiplatform.common.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
internal actual fun Modifier.extraPointerInput(scrollState: VicoScrollState): Modifier {
  var scrollJob by rememberWrappedValue<Job?>(null)
  val coroutineScope = rememberCoroutineScope()
  val animationSpec = rememberSplineBasedDecay<Float>()
  return draggable(
    state =
      rememberDraggableState { delta ->
        scrollJob?.cancel()
        scrollJob = coroutineScope.launch { scrollState.scroll(Scroll.Relative.pixels(delta)) }
      },
    orientation = Orientation.Horizontal,
    onDragStopped = { velocity ->
      scrollJob?.cancel()
      scrollJob =
        coroutineScope.launch {
          AnimationState(scrollState.value, velocity).animateDecay(animationSpec) {
            launch { scrollState.scroll(Scroll.Absolute.pixels(value)) }
          }
        }
    },
    reverseDirection = true,
  )
}
