package me.mauricee.pontoon.tv.launch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.mauricee.pontoon.tv.ui.createScreen
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.launch.LaunchEvent
import me.mauricee.pontoon.ui.launch.LaunchViewModel

@AndroidEntryPoint
class LaunchFragment : Fragment() {

    private val viewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events.onEach { event ->
            val directions = when (event) {
                is LaunchEvent.ToLogin -> LaunchFragmentDirections.actionSplashFragmentToLoginFragment()
                LaunchEvent.ToSession -> TODO()
            }
            findNavController().navigate(directions)
        }.launchIn(lifecycleScope)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = createScreen {
        val state by viewModel.state.collectAsState(UiState.Empty)
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}