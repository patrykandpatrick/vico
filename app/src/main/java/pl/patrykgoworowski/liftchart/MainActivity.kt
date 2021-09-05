package pl.patrykgoworowski.liftchart

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import pl.patrykgoworowski.liftchart.databinding.ActivityMainBinding
import pl.patrykgoworowski.liftchart.extension.enableEdgeToEdge
import pl.patrykgoworowski.liftchart.extension.statusBarInsets
import pl.patrykgoworowski.liftchart.ui.addOnTabSelectedListener
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.path.RoundedCornersShape
import pl.patrykgoworowski.liftchart_common.path.toDrawable

class MainActivity : AppCompatActivity() {

    private val tabIndicator: Drawable by lazy {
        RoundedCornersShape(topLeftPercent = 100, topRightPercent = 100)
            .toDrawable(intrinsicHeight = 3.dp.toInt())
    }

    private val navigator = Navigator(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) navigator.navigateTo(Navigator.Screen.ComposeShowcase)

        with(binding) {
            tabLayout.apply {
                setOnApplyWindowInsetsListener { view, insets ->
                    view.updatePadding(top = insets.statusBarInsets.top)
                    insets
                }
                setSelectedTabIndicator(tabIndicator)
                addTab(newTab().setText(R.string.showcase_compose_title))
                addTab(newTab().setText(R.string.showcase_view_title))
                addOnTabSelectedListener { tab ->
                    val screen = when (tab.position) {
                        0 -> Navigator.Screen.ComposeShowcase
                        1 -> Navigator.Screen.ViewShowcase
                        else -> throw IllegalArgumentException()
                    }
                    navigator.navigateTo(screen)
                }
            }

        }
    }

}