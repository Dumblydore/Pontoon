package me.mauricee.pontoon.main.search

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_search.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchFragment : BaseFragment<SearchPresenter>(), SearchContract.View {

    @Inject
    lateinit var adapter: SearchResultAdapter

    override val actions: Observable<SearchContract.Action>
        get() = Observable.merge(adapter.actions,
                RxSearchView.queryTextChanges(search_view)
                        .sample(250, TimeUnit.MILLISECONDS)
                        .map { SearchContract.Action.Query(it.toString()) }
        )

    override fun getLayoutId(): Int = R.layout.fragment_search

    override fun updateState(state: SearchContract.State) {
        when (state) {
            is SearchContract.State.Loading -> search_container_lazy.state = LazyLayout.LOADING
            is SearchContract.State.Results -> {
                adapter.update(state)
                search_container_lazy.state = LazyLayout.SUCCESS
            }
            else -> search_container_lazy.state = LazyLayout.ERROR
        }
    }
}