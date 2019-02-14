package me.mauricee.pontoon.main.creator

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_creator.*
import kotlinx.android.synthetic.main.fragment_creator.view.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.VideoPageAdapter
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.preferences.darken
import me.mauricee.pontoon.rx.animator.updates
import me.mauricee.pontoon.rx.glide.toPalette
import javax.inject.Inject

class CreatorFragment : BaseFragment<CreatorPresenter>(), CreatorContract.View {

    private val creator by lazy { arguments!!.getString(CreatorKey) }
    private val primaryColor by lazy { ResourcesCompat.getColor(resources, R.color.colorPrimary, null) }
    @Inject
    lateinit var videoAdapter: VideoPageAdapter

    private val refreshes
        get() = RxSwipeRefreshLayout.refreshes(creator_container).map { CreatorContract.Action.Refresh(creator, true) }

    override val actions: Observable<CreatorContract.Action>
        get() = Observable.merge(refreshes, videoAdapter.actions.map(CreatorContract.Action::PlayVideo))
                .startWith(CreatorContract.Action.Refresh(creator, false))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = ChangeBounds()
        exitTransition = Fade().apply { startDelay = 150 }
        enterTransition = Fade()
        postponeEnterTransition()
    }

    override fun getLayoutId(): Int = R.layout.fragment_creator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.creator_list.layoutManager = LinearLayoutManager(requireContext())
        view.creator_list.adapter = videoAdapter
        creator_container_lazy.setupWithSwipeRefreshLayout(creator_container)
    }

    override fun updateState(state: CreatorContract.State) = when (state) {
        is CreatorContract.State.Loading -> creator_container_lazy.state = LazyLayout.LOADING
        is CreatorContract.State.DisplayCreator -> displayCreator(state.creator)
        is CreatorContract.State.DisplayVideos -> displayVideos(state.videos)
        is CreatorContract.State.Error -> processError(state)
        CreatorContract.State.Fetching -> creator_list_progress.isVisible = true
        CreatorContract.State.Fetched -> creator_list_progress.isVisible = false
    }

    private fun displayCreator(creator: UserRepository.Creator) {
        subscriptions += GlideApp.with(this).asBitmap().load(creator.user.profileImage)
                .error(R.drawable.ic_default_thumbnail)
                .toPalette().map { it.getVibrantColor(primaryColor) }
                .flatMapObservable { ValueAnimator.ofArgb(primaryColor, it).apply { startDelay = 250 }.updates() }
                .map { it.animatedValue as Int }
                .subscribe { it ->
                    creator_toolbar.setBackgroundColor(it)
                    requireActivity().window.statusBarColor = it.darken(.7f)
                }
        creator_toolbar.title = creator.name
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun displayVideos(videos: PagedList<Video>) {
        creator_container_lazy.state = LazyLayout.SUCCESS
        videoAdapter.submitList(videos)
    }

    private fun processError(error: CreatorContract.State.Error) {
        creator_container_lazy.errorText = getString(error.type.msg)
        creator_container_lazy.state = LazyLayout.ERROR
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.statusBarColor = requireActivity().primaryDarkColor
    }

    companion object {
        private const val CreatorKey = "Creator"
        fun newInstance(creator: String): Fragment = CreatorFragment().apply {
            arguments = bundleOf(CreatorKey to creator)
        }
    }
}