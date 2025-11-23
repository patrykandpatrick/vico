package com.patrykandpatrick.vico.core.common

import androidx.annotation.RestrictTo

/** Represents a size. */
public interface Size {
  /** The width (in pixels). */
  public val width: Float

  /** The height (in pixels). */
  public val height: Float
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public data class MutableSize(override var width: Float = 0f, override var height: Float = 0f) :
  Size
