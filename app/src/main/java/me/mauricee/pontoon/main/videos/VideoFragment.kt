package me.mauricee.pontoon.main.videos

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_videos.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.rx.lazylayout.retries
import javax.inject.Inject

class VideoFragment : BaseFragment<VideoPresenter>(), VideoContract.View {

    override fun getLayoutId(): Int = R.layout.fragment_videos

    @Inject
    lateinit var videoAdapter: SubscriptionVideoAdapter

    private val miscActions = PublishRelay.create<VideoContract.Action>()
    private val refreshes
        get() = Observable.merge(RxSwipeRefreshLayout.refreshes(videos_container),
                videos_container_lazy.retries())
                .doOnNext { videoAdapter.submitList(null) }
                .map { VideoContract.Action.Refresh(true) }

    override val actions: Observable<VideoContract.Action>
        get() = Observable.merge(refreshes, miscActions,
                videoAdapter.actions.map(VideoContract.Action::PlayVideo),
                videoAdapter.subscriptionAdapter.actions)
                .startWith(VideoContract.Action.Refresh(false))
                .mergeWith(RxToolbar.navigationClicks(videos_toolbar).map { VideoContract.Action.NavMenu })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videos_list.layoutManager = LayoutManager(requireContext())
        videos_list.adapter = videoAdapter
        videos_list.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        videos_container_lazy.setupWithSwipeRefreshLayout(videos_container)
    }

    override fun updateState(state: VideoContract.State) {
        when (state) {
            is VideoContract.State.Loading -> {
                if (state.clean) {
                    videoAdapter.submitList(null)
                    videos_container_lazy.state = LazyLayout.LOADING
                    videos_container.isRefreshing = true
                } else {
                    videos_page_progress.isVisible = true
                }
            }
            is VideoContract.State.DisplayVideos -> displayVideos(state.videos)
            is VideoContract.State.Error -> processError(state)
            is VideoContract.State.DisplaySubscriptions -> videoAdapter.subscriptionAdapter.user = state.subscriptions
            is VideoContract.State.FinishPageFetch -> videos_page_progress.isVisible = false
            is VideoContract.State.FetchError -> processFetchError(state)
        }
    }

    override fun reset() {
        videos_list.smoothScrollToPosition(0)
    }

    private fun displayVideos(videos: PagedList<Video>) {
        videoAdapter.submitList(videos)
        videos_container_lazy.state = LazyLayout.SUCCESS
    }

    private fun processError(error: VideoContract.State.Error) {
        videos_container_lazy.errorView
                ?.findViewById<TextView>(R.id.lazy_error_text)
                ?.setText(error.type.msg)
        videos_container_lazy.state = LazyLayout.ERROR
    }

    private fun processFetchError(error: VideoContract.State.FetchError) {
        Snackbar.make(view!!, error.type.msg, Snackbar.LENGTH_LONG)
                .also {
                    if (error.type != VideoContract.State.FetchError.Type.NoVideos)
                        it.setAction(R.string.retry) { error.retry() }
                }.show()
    }

    private class LayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun isAutoMeasureEnabled(): Boolean = true
    }
}