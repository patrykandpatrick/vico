package pl.patrykgoworowski.liftchart_view

import androidx.annotation.Dimension

/**
 * Denotes that an number parameter, field or method return value is expected
 * to represent a density-independent pixels dimension.
 */
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.BINARY)
@kotlin.annotation.Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.LOCAL_VARIABLE)
@Dimension(unit = Dimension.DP)
annotation class Dp