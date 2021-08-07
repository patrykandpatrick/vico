package pl.patrykgoworowski.liftchart

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import pl.patrykgoworowski.liftchart.showcase.ComposeShowcaseFragment
import pl.patrykgoworowski.liftchart.showcase.ViewShowcaseFragment

class Navigator(
    private val activity: FragmentActivity,
    @IdRes private val containerId: Int = R.id.fragment_view,
) {

    enum class Screen {
        ComposeShowcase,
        ViewShowcase
    }

    fun navigateTo(screen: Screen) {
        activity.supportFragmentManager
            .beginTransaction()
            .replace(containerId, createFragment(screen))
            .commit()
    }

    private fun createFragment(screen: Screen): Fragment = when (screen) {
        Screen.ComposeShowcase -> ComposeShowcaseFragment()
        Screen.ViewShowcase -> ViewShowcaseFragment()
    }

}