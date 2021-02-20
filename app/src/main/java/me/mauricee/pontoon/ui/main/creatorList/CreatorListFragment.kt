package me.mauricee.pontoon.ui.main.creatorList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.common.SpaceItemDecoration
import me.mauricee.pontoon.databinding.FragmentCreatorListBinding
import me.mauricee.pontoon.databinding.ItemCreatorCardBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.creator.Creator
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.main.creatorList.CreatorListFragmentDirections.actionGlobalCreatorFragment

@AndroidEntryPoint
class CreatorListFragment : BaseFragment(R.layout.fragment_creator_list) {

    private val viewModel: CreatorListContract.ViewModel by viewModels()
    private val binding by viewBinding(FragmentCreatorListBinding::bind)
    private val creatorAdapter = SimpleBindingAdapter(ItemCreatorCardBinding::inflate, ::bindCreator)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NavigationUI.setupWithNavController(binding.creatorListToolbar, findNavController())

        binding.creatorListList.apply {
            adapter = creatorAdapter
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
        }

        subscriptions += creatorAdapter.clicks.map { CreatorListContract.Action.CreatorSelected(it.model) }
                .subscribe { viewModel.sendAction(it) }
        subscriptions += binding.creatorListContainer.refreshes().subscribe {
            viewModel.sendAction(CreatorListContract.Action.Refresh)
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is CreatorListContract.Event.DisplayUnsubscribedPrompt -> Snackbar.make(binding.root, getString(R.string.creator_list_unsubscribed), Snackbar.LENGTH_SHORT).show()
                is CreatorListContract.Event.NavigateToCreator -> {
                    findNavController().navigate(actionGlobalCreatorFragment(event.creator.id))
                }
            }
        }

        viewModel.state.mapDistinct(CreatorListContract.State::creators)
                .observe(viewLifecycleOwner, creatorAdapter::submitList)
        viewModel.state.mapDistinct { it.uiState.lazyState() }
                .observe(viewLifecycleOwner) { binding.creatorListContainerLazy.state = it }
        viewModel.state.mapDistinct { it.uiState.error }.notNull()
                .observe(viewLifecycleOwner) { binding.creatorListContainerLazy.errorText = it.text(requireContext()) }
        viewModel.state.map { it.uiState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.creatorListContainer.isRefreshing = it
        }
    }

    private fun bindCreator(binding: ItemCreatorCardBinding, creator: Creator) {
        binding.itemTitle.text = creator.entity.name
        GlideApp.with(binding.root).load(creator.user.profileImage)
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.itemIconBig)
    }


    companion object {
        fun newInstance() = CreatorListFragment()
    }

}