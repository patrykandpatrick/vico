---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/component
---

# Component

[`Component`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-component/)s are the basic graphical building blocks of Vico. There are several built-in implementations, which cover most use cases:

* [`TextComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-text-component/) draws text. It supports font customization, line breaks, rotation, backgrounds, and more. In composable contexts, use [`rememberTextComponenent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-text-component).
* [`ShapeComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-shape-component/) draws [`Shape`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.shape/-shape/)s, for which you can define a color, a stroke, and more. In composable contexts, use [`rememberShapeComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-shape-component).
* [`LineComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-line-component/) draws horizontal and vertical lines. In composable contexts, use [`rememberLineComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-line-component).
* [`LayeredComponent`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-layered-component/?query=open%20class%20LayeredComponent\(back:%20Component,%20front:%20Component,%20padding:%20Insets%20=%20Insets.Zero,%20margins:%20Insets%20=%20Insets.Zero\)%20:%20Component) draws two `Component`s on top of each other and lets you specify their spacing.
