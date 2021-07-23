package pl.patrykgoworowski.liftchart.ui

import com.google.android.material.tabs.TabLayout

inline fun TabLayout.addOnTabSelectedListener(
    crossinline onTabSelected: (TabLayout.Tab) -> Unit,
) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onTabSelected(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabReselected(tab: TabLayout.Tab?) {}
    })
}