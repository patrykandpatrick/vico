# Component

[`Component`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-component/) instances are the basic graphical building blocks of Vico. There are several built-in implementations, which cover most use cases:

* [`TextComponent`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-text-component/) draws text. It supports font customization, line breaks, rotation, backgrounds, and more.
* [`ShapeComponent`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-shape-component/) draws [`Shape`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.shape/-shape/) instances, for which you can define a color, a stroke, and more.
* [`LineComponent`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.component/-line-component/), which is a type of `ShapeComponent`, draws horizontal and vertical lines.
* [`LayeredComponent`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common/-layered-component/) draws two `Component` instances on top of each other and lets you specify their spacing.
