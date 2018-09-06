package me.mauricee.pontoon.main.search

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_search.*
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
                RxSearchView.queryTextChanges(search_view)
                        .sample(250, TimeUnit.MILLISECONDS)
                        .doOnNext { adapter.submitList(null) }
                        .map { SearchContract.Action.Query(it.toString()) }
        )

    override fun getLayoutId(): Int = R.layout.fragment_search

    override fun updateState(state: SearchContract.State) {
        when (state) {
            is SearchContract.State.Loading -> search_container_lazy.state = LazyLayout.LOADING
            is SearchContract.State.Results -> {
                adapter.submitList(state.list)
                search_container_lazy.state = LazyLayout.SUCCESS
            }
            else -> search_container_lazy.state = LazyLayout.ERROR
        }
    }
}