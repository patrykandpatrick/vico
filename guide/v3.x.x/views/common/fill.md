---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/common/fill
---

# Fill

## Overview

[`Fill`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common/-fill/) defines fill properties—either a color or a [`ShaderProvider`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/) instance.

## `ShaderProvider`

[`ShaderProvider`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/) lets you create complex fills, including gradients and patterns. `core` has the following factory functions:

* [`ShaderProvider.bitmap`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/bitmap)
* [`ShaderProvider.component`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/component)
* [`ShaderProvider.compose`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/compose)
* [`ShaderProvider.horizontalGradient`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/horizontal-gradient)
* [`ShaderProvider.verticalGradient`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/-shader-provider/-companion/vertical-gradient)
* [`Shader.toShaderProvider`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shader/to-shader-provider)
