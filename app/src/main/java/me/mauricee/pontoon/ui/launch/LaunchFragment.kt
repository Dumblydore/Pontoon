package me.mauricee.pontoon.ui.launch

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentLaunchBinding
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.launch.LaunchFragmentDirections.actionLaunchFragmentToLoginGraph
import me.mauricee.pontoon.ui.launch.LaunchFragmentDirections.actionLaunchFragmentToMainFragment
import javax.inject.Inject

@AndroidEntryPoint
class LaunchFragment @Inject constructor() : BaseFragment(R.layout.fragment_launch) {

    private val viewModel: me.mauricee.pontoon.ui.LaunchViewModel by viewModels()
    private val binding by viewBinding(FragmentLaunchBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events.onEach {
            val directions = when (it) {
                is me.mauricee.pontoon.ui.LaunchEvent.ToLogin -> actionLaunchFragmentToLoginGraph(it.initializeWith2Fa)
                me.mauricee.pontoon.ui.LaunchEvent.ToSession -> actionLaunchFragmentToMainFragment()
            }
            findNavController().navigate(directions)
        }.launchIn(lifecycleScope)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.map { it.isLoading() }.distinctUntilChanged().onEach {
            if (it) binding.launchProgress.show()
            else binding.launchProgress.hide()
        }.launchIn(lifecycleScope)
    }
}