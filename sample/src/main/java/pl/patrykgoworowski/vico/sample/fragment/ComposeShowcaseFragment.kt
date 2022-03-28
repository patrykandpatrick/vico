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

package pl.patrykgoworowski.vico.sample.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import pl.patrykgoworowski.vico.databinding.FragmentComposeShowcaseBinding
import pl.patrykgoworowski.vico.sample.viewmodel.ShowcaseViewModel
import pl.patrykgoworowski.vico.sample.compose.component.ComposeShowcase
import pl.patrykgoworowski.vico.sample.extensions.navigationBarInsets

internal class ComposeShowcaseFragment : Fragment() {

    private var _binding: FragmentComposeShowcaseBinding? = null
    private val binding get() = _binding!!

    private val showcaseViewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComposeShowcaseBinding.inflate(layoutInflater)
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
