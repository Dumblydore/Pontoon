package me.mauricee.pontoon.main.creatorList

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_creator_list.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.SpaceItemDecoration
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
        creatorList_list.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
    }

    override fun updateState(state: CreatorListContract.State) {
        when (state) {
            CreatorListContract.State.Loading -> creatorList_container_lazy.state = LazyLayout.LOADING
            is CreatorListContract.State.DisplayCreators -> {
                adapter.submitList(state.creator)
                creatorList_container_lazy.state = LazyLayout.SUCCESS
            }
            is CreatorListContract.State.Error -> {
                when (state.type) {
                    CreatorListContract.State.Error.Type.Unsubscribed -> Snackbar.make(view!!, getString(R.string.creator_list_unsubscribed), Snackbar.LENGTH_SHORT).show()
                    CreatorListContract.State.Error.Type.Network,
                    CreatorListContract.State.Error.Type.Unknown -> {
                        creatorList_container_lazy.state = LazyLayout.ERROR
                        creatorList_container_lazy.errorText = getString(state.type.msg)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = CreatorListFragment()
    }

}