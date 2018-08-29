package me.mauricee.pontoon.main.videos

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_videos.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.main.VideoAdapter
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class VideoFragment : BaseFragment<VideoPresenter>(), VideoContract.View {

    override fun getLayoutId(): Int = R.layout.fragment_videos

    @Inject
    lateinit var videoAdapter: VideoAdapter
    @Inject
    lateinit var subscriptionAdapter: SubscriptionAdapter

    private val refreshes
        get() = RxSwipeRefreshLayout.refreshes(videos_container)
                .map { VideoContract.Action.Refresh }

    override val actions: Observable<VideoContract.Action>
        get() = Observable.merge(refreshes,
                videoAdapter.actions.map(VideoContract.Action::PlayVideo), subscriptionAdapter.actions)
                .startWith(VideoContract.Action.Refresh)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videos_list.layoutManager = LayoutManager(requireContext())
        videos_list.adapter = videoAdapter

        videos_subscriptions.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false)
        videos_subscriptions.adapter = subscriptionAdapter
        videos_container_lazy.setupWithSwipeRefreshLayout(videos_container)
    }

    override fun updateState(state: VideoContract.State) = when (state) {
        is VideoContract.State.Loading -> {
            videos_container_lazy.state = LazyLayout.LOADING
            videos_container.isRefreshing = true
        }
        is VideoContract.State.DisplayVideos -> displayVideos(state.videos)
        is VideoContract.State.Error -> processError(state)
        is VideoContract.State.DisplaySubscriptions -> subscriptionAdapter.user = state.subscriptions
    }

    private fun displayVideos(videos: List<Video>) {
        videoAdapter.videos = videos
        videos_container_lazy.state = LazyLayout.SUCCESS
    }

    private fun processError(error: VideoContract.State.Error) {
        videos_container_lazy.errorView
                ?.findViewById<TextView>(R.id.lazy_error_text)
                ?.setText(error.type.msg)
        videos_container_lazy.state = LazyLayout.ERROR
    }

    private class LayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun isAutoMeasureEnabled(): Boolean = true
    }
}