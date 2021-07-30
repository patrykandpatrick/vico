package pl.patrykgoworowski.liftchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import pl.patrykgoworowski.liftchart.databinding.ActivityMainBinding
import pl.patrykgoworowski.liftchart.extension.enableEdgeToEdge
import pl.patrykgoworowski.liftchart.extension.statusBarInsets
import pl.patrykgoworowski.liftchart.showcase.ShowcaseFragmentAdapter
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.path.roundedCornersShape
import pl.patrykgoworowski.liftchart_common.path.toDrawable

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            viewPager.adapter = ShowcaseFragmentAdapter(this@MainActivity)

            tabLayout.setSelectedTabIndicator(
                roundedCornersShape(topLeftPercent = 100, topRightPercent = 100)
                    .toDrawable(intrinsicHeight = 3.dp.toInt())
            )

            topBar.apply {
                setOnApplyWindowInsetsListener { view, insets ->
                    view.updatePadding(top = insets.statusBarInsets.top)
                    insets
                }
                background =
                    MaterialShapeDrawable.createWithElevationOverlay(this@MainActivity).apply {
                        elevation = topBar.elevation
                    }
            }

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.setText(R.string.showcase_compose_title)
                    1 -> tab.setText(R.string.showcase_view_title)
                }
            }.attach()
        }
    }

}