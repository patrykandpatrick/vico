# Fill

[`Fill`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common/-fill/) defines fill properties—either a color or a [`ShaderProvider`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/) instance.

## `ShaderProvider`

[`ShaderProvider`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/) lets you create complex fills, including gradients and patterns. `core` has the following factory functions:

* [`ShaderProvider.bitmap`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/bitmap)
* [`ShaderProvider.component`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/component)
* [`ShaderProvider.compose`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/compose)
* [`ShaderProvider.horizontalGradient`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/horizontal-gradient)
* [`ShaderProvider.verticalGradient`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/vertical-gradient)
* [`Shader.toShaderProvider`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/to-shader-provider)
