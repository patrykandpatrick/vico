package com.patrykandpatrick.vico.core.common

import androidx.annotation.RestrictTo

/** Represents an offset. */
public interface Offset {
  /** The _x_-offset (in pixels). */
  public val x: Float

  /** The _y_-offset (in pixels). */
  public val y: Float

  /** @suppress */
  @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
  public companion object {
    public val Zero: Offset = MutableOffset()
  }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public data class MutableOffset(override var x: Float = 0f, override var y: Float = 0f) : Offset
