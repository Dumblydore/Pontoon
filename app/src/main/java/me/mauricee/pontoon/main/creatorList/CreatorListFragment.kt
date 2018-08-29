package me.mauricee.pontoon.main.creatorList

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_creator_list.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import javax.inject.Inject

class CreatorListFragment : BaseFragment<CreatorListPresenter>(), CreatorListContract.View {

    @Inject
    lateinit var adapter: CreatorListAdapter

    override val actions: Observable<CreatorListContract.Action>
        get() = adapter.actions

    override fun getLayoutId(): Int = R.layout.fragment_creator_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        creatorList_list.layoutManager = GridLayoutManager(requireContext(), 2)
        creatorList_list.adapter = adapter
    }

    override fun updateState(state: CreatorListContract.State) = when (state) {
        CreatorListContract.State.Loading -> creatorList_container_lazy.state = LazyLayout.LOADING
        is CreatorListContract.State.DisplayCreators -> {
            adapter.creators = state.creator
            creatorList_container_lazy.state = LazyLayout.SUCCESS
        }
        is CreatorListContract.State.Error -> {
            creatorList_container_lazy.state = LazyLayout.ERROR
        }
    }

    companion object {
        fun newInstance() = CreatorListFragment()
    }

}