package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader

public abstract class CacheableDynamicShader : DynamicShader {

    private val cache = HashMap<String, Shader>(1)

    override fun provideShader(
        bounds: RectF
    ): Shader {
        val cacheKey = createKey(bounds)
        return cache[cacheKey] ?: createShader(bounds).also { gradient ->
            cache.clear()
            cache[cacheKey] = gradient
        }
    }

    public abstract fun createShader(bounds: RectF): Shader

    private fun createKey(bounds: RectF): String =
        "%s,%s,%s,%s".format(bounds.left, bounds.top, bounds.right, bounds.bottom)
}