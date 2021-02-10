package me.mauricee.pontoon.ui.launch

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentLaunchBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.launch.LaunchFragmentDirections.actionLaunchFragmentToLoginGraph
import me.mauricee.pontoon.ui.launch.LaunchFragmentDirections.actionLaunchFragmentToMainFragment
import javax.inject.Inject

@AndroidEntryPoint
class LaunchFragment @Inject constructor() : BaseFragment(R.layout.fragment_launch) {

    private val viewModel: LaunchViewModel by viewModels()
    private val binding by viewBinding(FragmentLaunchBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events.observe(this) {
            val directions = when (it) {
                is LaunchEvent.ToLogin -> actionLaunchFragmentToLoginGraph(it.initializeWith2Fa)
                LaunchEvent.ToSession -> actionLaunchFragmentToMainFragment()
            }
            findNavController().navigate(directions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.mapDistinct { it.uiState.isLoading() }.observe(viewLifecycleOwner) {
            if (it) binding.launchProgress.show()
            else binding.launchProgress.hide()
        }
    }
}