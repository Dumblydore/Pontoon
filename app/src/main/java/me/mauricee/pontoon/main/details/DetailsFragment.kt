package me.mauricee.pontoon.main.details

import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_details.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class DetailsFragment : BaseFragment<DetailsPresenter>(), DetailsContract.View {

    @Inject
    lateinit var relatedVideosAdapter: RelatedVideoAdapter
    @Inject
    lateinit var commentsAdapter: CommentAdapter
    @Inject
    lateinit var formatter: DateTimeFormatter

    lateinit var creator: UserRepository.Creator

    override fun getLayoutId(): Int = R.layout.fragment_details

    override val actions: Observable<DetailsContract.Action>
        get() = Observable.merge(
                commentsAdapter.actions,
                relatedVideosAdapter.actions,
                player_small_icon.clicks().map { DetailsContract.Action.ViewCreator(creator) })//,
//                player_progress.changeEvents().map(DetailsContract. Action::SeekTo))
                .startWith(DetailsContract.Action.PlayVideo(arguments!!.getString(VideoKey)))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        player_relatedVideos.adapter = relatedVideosAdapter
        player_relatedVideos.layoutManager = LinearLayoutManager(requireContext())

        player_comments.adapter = commentsAdapter
        player_comments.layoutManager = LinearLayoutManager(requireContext())

        subscriptions += player_info.clicks().subscribe {
            player_description.apply { isVisible = !isVisible }
            player_description_divider.isVisible = player_description.isVisible
            player_releaseDate.isVisible = player_description.isVisible
        }
    }

    override fun updateState(state: DetailsContract.State) {
        when (state) {
            is DetailsContract.State.Loading -> {
                details.setState(LazyLayout.LOADING, false)
            }
            is DetailsContract.State.Progress -> {
                player_progress.progress = state.progress
                player_progress.secondaryProgress = state.bufferedProgress
            }
            is DetailsContract.State.VideoInfo -> {
                details.state = LazyLayout.SUCCESS
                displayVideoInfo(state.video)
            }
            is DetailsContract.State.Error -> {
                details.state = LazyLayout.ERROR
            }
            is DetailsContract.State.Comments -> {
                commentsAdapter.comments = state.comments
                scrollToSelectedComment()
            }
            is DetailsContract.State.RelatedVideos -> relatedVideosAdapter.videos = state.relatedVideos
            is DetailsContract.State.Duration -> player_progress.max = state.duration
            is DetailsContract.State.PlaybackState -> {
            }
        }
    }

    private fun scrollToSelectedComment() {
        arguments?.getString(CommentKey, "")?.apply {
            if (isNotBlank()) {
                val commentIndex = commentsAdapter.comments.indexOfFirst { it.id == this }
                        .let { player_comments.getChildAt(it).y.toInt() }
                player_details.smoothScrollTo(0, commentIndex)
            }
        }
    }

    private fun displayVideoInfo(info: Video) {
        player_details.scrollTo(0, 0)
        info.apply {
            player_title.text = title
            player_subtitle.text = creator.name
            player_description.text = description
            player_releaseDate.text = getString(R.string.player_releaseDate, formatter.format(releaseDate))
            Linkify.addLinks(player_description, Linkify.WEB_URLS)
            this@DetailsFragment.creator = creator
        }
        GlideApp.with(this).load(info.creator.user.profileImage).circleCrop()
                .placeholder(R.drawable.ic_default_thumbnail)
                .error(R.drawable.ic_default_thumbnail)
                .into(player_small_icon)
    }

    companion object {
        private const val VideoKey = "VideoKey"
        private const val CommentKey = "CommentKey"

        fun newInstance(videoId: String, commentId: String = ""): DetailsFragment =
                DetailsFragment().also {
                    it.arguments = Bundle().apply {
                        putString(VideoKey, videoId)
                        putString(CommentKey, commentId)
                    }
                }
    }
}