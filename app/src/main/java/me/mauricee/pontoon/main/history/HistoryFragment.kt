package me.mauricee.pontoon.main.history

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.lazy_error_layout.view.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.main.VideoPageAdapter
import javax.inject.Inject

class HistoryFragment : BaseFragment<HistoryPresenter>(), HistoryContract.View {

    @Inject
    lateinit var videoAdapter: VideoPageAdapter

    override val actions: Observable<HistoryContract.Action>
        get() = videoAdapter.actions.map(HistoryContract.Action::PlayVideo)

    override fun getLayoutId(): Int = R.layout.fragment_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        history_list.adapter = videoAdapter
        history_list.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun updateState(state: HistoryContract.State) = when (state) {
        is HistoryContract.State.Loading -> {}//history_container_lazy.state = LazyLayout.LOADING
        is HistoryContract.State.DisplayVideos -> {
            history_container_lazy.state = LazyLayout.SUCCESS
            videoAdapter.submitList(state.videos)
        }
        is HistoryContract.State.Error -> {
            history_container_lazy.state = LazyLayout.ERROR
            history_container_lazy.lazy_error_text.setText(state.type.msg)
        }
    }
}