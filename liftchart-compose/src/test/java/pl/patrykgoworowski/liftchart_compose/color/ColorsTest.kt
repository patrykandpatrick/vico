package pl.patrykgoworowski.liftchart_compose.color

import androidx.compose.ui.graphics.Color
import org.junit.Test
import pl.patrykgoworowski.liftchart_compose.extension.color
import pl.patrykgoworowski.liftchart_compose.extension.colorInt
import kotlin.test.assertEquals

/**
 * Test conversion from classic Android color int to Jetpack Compose's [Color] and back.
 */
class ColorsTest {

    private val baseColor: Long = 0xFF00FF00
    private val colorInt: Int = baseColor.toInt()
    private val jcColor: Color = Color(baseColor)

    @Test
    fun `convert jetpack compose color to int`() {
        assertEquals(jcColor.colorInt, colorInt)
    }

    @Test
    fun `convert jetpack compose color to int and back`() {
        assertEquals(jcColor, jcColor.colorInt.color)
    }

}