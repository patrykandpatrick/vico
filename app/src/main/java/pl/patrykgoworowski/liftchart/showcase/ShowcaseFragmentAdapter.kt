package pl.patrykgoworowski.liftchart.showcase

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShowcaseFragmentAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ComposeShowcaseFragment()
        1 -> ViewShowcaseFragment()
        else -> throw IllegalArgumentException("Got illegal position $position.")
    }
}