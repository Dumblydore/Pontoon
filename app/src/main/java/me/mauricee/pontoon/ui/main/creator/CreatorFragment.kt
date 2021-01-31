package me.mauricee.pontoon.ui.main.creator

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.FragmentCreatorBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.VideoPageAdapter
import javax.inject.Inject

class CreatorFragment : NewBaseFragment(R.layout.fragment_creator) {

    @Inject
    lateinit var videoAdapter: VideoPageAdapter

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var factory: CreatorContract.ViewModel.Factory
    private val viewModel: CreatorContract.ViewModel by viewModels { factory }
    private val binding by viewBinding(FragmentCreatorBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.creatorList.adapter = videoAdapter

        viewModel.state.mapDistinct(CreatorContract.State::creator)
                .notNull().observe(viewLifecycleOwner, ::displayCreator)

        viewModel.state.mapDistinct { it.screenState.lazyState() }
                .observe(viewLifecycleOwner) { binding.creatorContainerLazy.state = it }
        viewModel.state.map { it.screenState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.creatorContainer.isRefreshing = it
        }
        viewModel.state.mapDistinct { it.screenState.error }.notNull().observe(viewLifecycleOwner) {
            binding.creatorContainerLazy.errorText = it.message?.let(::getString)
                    ?: it.exception?.message
        }
        viewModel.state.mapDistinct { it.videos }.observe(viewLifecycleOwner, videoAdapter::submitList)

        subscriptions += binding.creatorContainer.refreshes().subscribe {
            viewModel.sendAction(CreatorContract.Action.Refresh)
        }
        subscriptions += videoAdapter.actions.subscribe {
            viewModel.sendAction(CreatorContract.Action.PlayVideo(it))
        }
    }

    private fun displayCreator(creator: Creator) {
        binding.creatorToolbar.title = creator.entity.name
    }

    companion object {
        internal const val CreatorIdKey = "CreatorId"
        fun newInstance(creatorId: String): Fragment = CreatorFragment().apply {
            arguments = bundleOf(CreatorIdKey to creatorId)
        }
    }
}