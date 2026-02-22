---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/common/component
---

# Component

[`Component`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-component/)s are the basic graphical building blocks of Vico. There are several built-in implementations, which cover most use cases:

* [`TextComponent`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-text-component/) draws text. It supports font customization, line breaks, rotation, backgrounds, and more.
* [`ShapeComponent`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-shape-component/) draws [`Shape`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shape/-shape/)s, for which you can define a color, a stroke, and more.
* [`LineComponent`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-line-component/), which is a type of `ShapeComponent`, draws horizontal and vertical lines.
* [`LayeredComponent`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common/-layered-component/) draws two `Component`s on top of each other and lets you specify their spacing.
