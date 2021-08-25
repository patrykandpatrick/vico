package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader

abstract class CacheableDynamicShader : DynamicShader {

    private val cache = HashMap<String, Shader>(1)

    override fun provideShader(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        parentBounds: RectF
    ): Shader {
        val cacheKey = createKey(parentBounds)
        return cache[cacheKey] ?: createShader(parentBounds).also { gradient ->
            cache.clear()
            cache[cacheKey] = gradient
        }
    }

    abstract fun createShader(parentBounds: RectF): Shader

    private fun createKey(bounds: RectF): String =
        "%s,%s,%s,%s".format(bounds.left, bounds.top, bounds.right, bounds.bottom)
}