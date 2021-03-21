package pl.patrykgoworowski.liftchart_common

import android.graphics.Color
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.dp

val DEF_LABEL_COMPONENT: TextComponent
    get() = TextComponent()
        .apply {
            setPadding(all = 8f.dp)
        }

val DEF_AXIS_COMPONENT: RectComponent
    get() = RectComponent(Color.BLUE, 2f.dp)

val DEF_TICK_COMPONENT: TickComponent
    get() = TickComponent(Color.BLUE, 2f.dp)

val DEF_GUIDELINE_COMPONENT: GuidelineComponent
    get() = GuidelineComponent(Color.GRAY, 1f.dp)
