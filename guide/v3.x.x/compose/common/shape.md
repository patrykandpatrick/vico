---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/shape
---

# Shape

Vico’s Compose APIs use `Shape` instances. You can use Compose’s built-in implementations, including `RoundedCornerShape` and `CutCornerShape`, or provide custom shape implementations.

Vico provides the following additional `Shape` implementations:

* [`DashedShape`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-dashed-shape/) alternates a base `Shape` instance with gaps.
* [`MarkerCornerBasedShape`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-marker-corner-based-shape/) wraps a `CornerBasedShape` instance and adds a triangular marker tick.
