---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/component
---

# Component

[`Component`][component] instances are the basic graphical building blocks of Vico. There are several built-in implementations, which cover most use cases:

* [`TextComponent`][text-component] draws text. It supports font customization, line breaks, rotation, backgrounds, and more. In composable contexts, use [`rememberTextComponent`][remember-text-component].
* [`ShapeComponent`][shape-component] draws `Shape` instances, for which you can define a color, a stroke, and more. In composable contexts, use [`rememberShapeComponent`][remember-shape-component].
* [`LineComponent`][line-component] draws horizontal and vertical lines. In composable contexts, use [`rememberLineComponent`][remember-line-component].
* [`LayeredComponent`][layered-component] draws two `Component` instances on top of each other and lets you specify their spacing.

[component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-component/
[text-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-text-component/
[remember-text-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-text-component.html
[shape-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-shape-component/
[remember-shape-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-shape-component.html
[line-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/-line-component/
[remember-line-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common.component/remember-line-component.html
[layered-component]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-layered-component/?query=open%20class%20LayeredComponent\(back:%20Component,%20front:%20Component,%20padding:%20Insets%20=%20Insets.Zero,%20margins:%20Insets%20=%20Insets.Zero\)%20:%20Component
