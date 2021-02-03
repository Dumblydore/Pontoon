package me.mauricee.pontoon.ui.main.search

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v7.widget.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentSearchBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.UiState
import me.mauricee.pontoon.ui.main.VideoPageAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : NewBaseFragment(R.layout.fragment_search) {

    @Inject
    lateinit var adapter: VideoPageAdapter

    private val viewModel: SearchViewModel by viewModels()
    private val binding by viewBinding(FragmentSearchBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchList.adapter = adapter

        subscriptions += adapter.actions.map(SearchAction::VideoClicked)
                .subscribe(viewModel::sendAction)
        subscriptions += binding.searchView.queryTextChanges()
                .filter(CharSequence::isNotBlank)
                .map(CharSequence::toString)
                .map(SearchAction::Query)
                .debounce(250, TimeUnit.MILLISECONDS)
                .subscribe(viewModel::sendAction)


        viewModel.state.apply {
            mapDistinct(SearchState::videos).observe(viewLifecycleOwner, adapter::submitList)
            map { it.screenState.lazyState() }.observe(viewLifecycleOwner) {
                binding.searchContainerLazy.state = it
            }
            mapDistinct { it.pageState is UiState.Loading }.observe(viewLifecycleOwner) {
                binding.searchPageProgress.isVisible = it
            }
            mapDistinct { it.screenState.error }.notNull().observe(viewLifecycleOwner) {
                binding.searchContainerLazy.errorText = it.text(requireContext())
            }
            mapDistinct { it.pageState.error?.message }.notNull().observe(viewLifecycleOwner) {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}