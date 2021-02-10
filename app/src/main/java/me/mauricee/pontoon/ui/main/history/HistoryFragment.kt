package me.mauricee.pontoon.ui.main.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentHistoryBinding
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.main.VideoPageAdapter
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment(R.layout.fragment_history) {

    @Inject
    lateinit var videoAdapter: VideoPageAdapter
    private val viewModel: HistoryContract.ViewModel by viewModels()

    private val binding by viewBinding(FragmentHistoryBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.historyList.adapter = videoAdapter

        subscriptions += videoAdapter.actions.subscribe {
            viewModel.sendAction(HistoryContract.Action.PlayVideo(it))
        }

//        viewModel.state.mapDistinct(HistoryContract.State::videos)
//                .observe(viewLifecycleOwner, videoAdapter::submitList)
        viewModel.state.mapDistinct { it.uiState.lazyState() }
                .observe(viewLifecycleOwner) { binding.historyContainerLazy.state = it }
        viewModel.state.mapDistinct { it.uiState.error }.notNull()
                .observe(viewLifecycleOwner) { binding.historyContainerLazy.errorText = it.text(requireContext()) }
    }
}