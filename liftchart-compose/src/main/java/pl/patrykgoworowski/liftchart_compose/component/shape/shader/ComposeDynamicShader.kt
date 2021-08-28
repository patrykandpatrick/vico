package pl.patrykgoworowski.liftchart_compose.component.shape.shader

import android.graphics.BlendMode
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.RequiresApi
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader

@RequiresApi(Build.VERSION_CODES.Q)
fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: BlendMode,
): DynamicShader = DynamicShader { bounds ->
    ComposeShader(
        first.provideShader(bounds),
        second.provideShader(bounds),
        mode
    )
}

fun composeShader(
    first: DynamicShader,
    second: DynamicShader,
    mode: PorterDuff.Mode,
): DynamicShader = DynamicShader { bounds ->
    ComposeShader(
        first.provideShader(bounds),
        second.provideShader(bounds),
        mode
    )
}