/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatryk.vico.sample.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatryk.vico.databinding.FragmentComposeShowcaseBinding
import com.patrykandpatryk.vico.sample.compose.component.ComposeShowcase
import com.patrykandpatryk.vico.sample.extensions.navigationBarInsets
import com.patrykandpatryk.vico.sample.viewmodel.ShowcaseViewModel

internal class ComposeShowcaseFragment : Fragment() {

    private val showcaseViewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentComposeShowcaseBinding.inflate(inflater, container, false)
        with(binding) {
            wrapper.setOnApplyWindowInsetsListener { view, insets ->
                view.updatePadding(bottom = insets.navigationBarInsets.bottom)
                insets
            }
            composeView.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent { ComposeShowcase(showcaseViewModel = showcaseViewModel) }
            }
        }
        return binding.root
    }
}
