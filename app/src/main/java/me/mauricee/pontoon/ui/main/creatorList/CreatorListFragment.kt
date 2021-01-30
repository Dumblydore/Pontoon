package me.mauricee.pontoon.ui.main.creatorList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SpaceItemDecoration
import me.mauricee.pontoon.databinding.FragmentCreatorListBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.NewBaseFragment
import javax.inject.Inject

class CreatorListFragment : NewBaseFragment(R.layout.fragment_creator_list) {

    @Inject
    lateinit var creatorAdapter: CreatorListAdapter

    @Inject
    lateinit var factory: CreatorListContract.ViewModel.Factory
    private val viewModel: CreatorListContract.ViewModel by viewModels { factory }
    private val binding by viewBinding(FragmentCreatorListBinding::bind)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.creatorListList.apply {
            adapter = creatorAdapter
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
        }

        subscriptions += binding.creatorListContainer.refreshes().subscribe {
            viewModel.sendAction(CreatorListContract.Action.Refresh)
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

    companion object {
        fun newInstance() = CreatorListFragment()
    }

}