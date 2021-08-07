package pl.patrykgoworowski.liftchart_compose.gesture

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

typealias OnZoom = (centroid: Offset, zoomChange: Float) -> Unit

@Composable
fun rememberOnZoom(
    onZoom: OnZoom,
): OnZoom = remember { onZoom }

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.zoomable(
    onZoom: OnZoom,
    enabled: Boolean = true,
) = composed(
    factory = {
        val block: suspend PointerInputScope.() -> Unit = remember {
            {
                forEachGesture {
                    detectTransformGestures { centroid, _, zoom, _ ->
                        onZoom(centroid, zoom)
                    }
                }
            }
        }
        if (enabled) Modifier.pointerInput(Unit, block) else Modifier
    },
    inspectorInfo = debugInspectorInfo {
        name = "zoomable"
        properties["enabled"] = enabled
    }
)