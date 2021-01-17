package pl.patrykgoworowski.liftchart.extension

import android.content.Context
import androidx.core.content.ContextCompat

inline fun Context.color(resIdBlock: () -> Int): Int =
    ContextCompat.getColor(this, resIdBlock())

inline fun Context.colors(resIdsBlock: () -> IntArray): List<Int> =
    resIdsBlock().map { ContextCompat.getColor(this, it) }
