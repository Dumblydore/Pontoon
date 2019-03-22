package me.mauricee.pontoon.main.search

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.lazy_error_layout.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.main.VideoPageAdapter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchPresenter>(), SearchContract.View {

    @Inject
    lateinit var adapter: VideoPageAdapter

    override val actions: Observable<SearchContract.Action>
        get() = Observable.merge(adapter.actions.map(SearchContract.Action::PlayVideo),
                lazy_error_retry.clicks().map { SearchContract.Action.Query(search_view.query.toString()) },
                RxSearchView.queryTextChanges(search_view)
                        .debounce(250, TimeUnit.MILLISECONDS)
                        .map { SearchContract.Action.Query(it.toString()) }
        ).startWith(SearchContract.Action.Query(search_view.query.toString()))

    override fun getLayoutId(): Int = R.layout.fragment_search

    override fun getToolbar(): Toolbar? = search_toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_list.adapter = adapter
        search_list.layoutManager = LinearLayoutManager(requireActivity())
        if (search_view.query.isEmpty()) {
            search_view.isIconified = false
            search_view.requestFocusFromTouch()
        }
    }

    override fun updateState(state: SearchContract.State) = when (state) {
        is SearchContract.State.Loading -> {
            adapter.submitList(null)
            search_container_lazy.state = LazyLayout.LOADING
        }
        is SearchContract.State.FetchingPage -> search_page_progress.isVisible = true
        is SearchContract.State.Results -> {
            adapter.submitList(state.list)
            search_container_lazy.state = LazyLayout.SUCCESS
        }
        is SearchContract.State.Error -> {
            search_container_lazy.displayRetryButton = state.type != SearchContract.State.Type.NoText
            search_container_lazy.errorText = getString(state.type.msg)
            search_container_lazy.state = LazyLayout.ERROR
        }
        is SearchContract.State.FetchError -> {
            search_page_progress.isVisible = false
            Snackbar.make(view!!, state.type.msg, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { state.retry() }
                    .show()
        }
        is SearchContract.State.FinishFetching -> search_page_progress.isVisible = false
    }

    override fun reset() {
        search_list.smoothScrollToPosition(0)
    }
}