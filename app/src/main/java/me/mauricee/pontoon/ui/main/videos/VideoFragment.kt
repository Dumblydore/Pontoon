package me.mauricee.pontoon.ui.main.videos

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.support.v7.widget.navigationClicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentVideosBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.main.MainContract
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.videos.VideoFragmentDirections.actionVideoFragmentToCreatorFragment
import me.mauricee.pontoon.ui.main.videos.VideoFragmentDirections.actionVideoFragmentToCreatorListFragment
import javax.inject.Inject

@AndroidEntryPoint
class VideoFragment : NewBaseFragment(R.layout.fragment_videos) {

    @Inject
    lateinit var videoAdapter: SubscriptionVideoAdapter

    private val mainViewModel: MainContract.ViewModel by viewModels({ requireActivity() })
    private val playerViewModel: PlayerViewModel by viewModels({ requireActivity() })
    private val viewModel: VideoViewModel by viewModels()

    private val binding by viewBinding(FragmentVideosBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.events.observe(this) {
            val directions = when (it) {
                VideoEvent.NavigateToAllCreators -> actionVideoFragmentToCreatorListFragment()
                is VideoEvent.NavigateToCreator -> actionVideoFragmentToCreatorFragment(it.creatorId)
            }
            findNavController().navigate(directions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.videosList.apply {
            adapter = videoAdapter
            layoutManager = LayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        viewModel.state.mapDistinct(VideoState::subscriptions).observe(viewLifecycleOwner, videoAdapter.subscriptionAdapter::submitList)
        viewModel.state.mapDistinct(VideoState::videos).observe(viewLifecycleOwner, videoAdapter::submitList)

        viewModel.state.mapDistinct { it.screenState.error }.notNull().observe(viewLifecycleOwner) {
            binding.videosContainerLazy.errorText = it.text(requireContext())
        }
        viewModel.state.mapDistinct { it.screenState.lazyState() }.observe(viewLifecycleOwner) {
            binding.videosContainerLazy.state = it
        }
        viewModel.state.map { it.screenState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.videosContainer.isRefreshing = it
        }
        viewModel.state.map { it.pageState == UiState.Loading }.observe(viewLifecycleOwner) {
            binding.videosPageProgress.isVisible = it
        }

        subscriptions += binding.videosToolbar.navigationClicks()
                .map { MainContract.Action.ToggleMenu }
                .subscribe(mainViewModel::sendAction)
        subscriptions += binding.videosContainer.refreshes().map { VideoAction.Refresh }
                .subscribe(viewModel::sendAction)
        subscriptions += videoAdapter.actions.map { PlayerAction.PlayVideo(it.id) }
                .subscribe(playerViewModel::sendAction)
        subscriptions += videoAdapter.subscriptionAdapter.actions
                .subscribe(viewModel::sendAction)
    }

    private class LayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun isAutoMeasureEnabled(): Boolean = true
    }
}