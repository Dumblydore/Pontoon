package me.mauricee.pontoon.main.creator

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_creator.*
import kotlinx.android.synthetic.main.fragment_creator.view.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.ext.setStatusBarColor
import me.mauricee.pontoon.ext.supportActionBar
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.main.VideoPageAdapter
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.preferences.darken
import me.mauricee.pontoon.rx.glide.toPalette
import javax.inject.Inject

class CreatorFragment : BaseFragment<CreatorPresenter>(), CreatorContract.View {

    private val creatorId by lazy { arguments!!.getString(CreatorIdKey) }
    private val creatorName by lazy { arguments!!.getString(CreatorNameKey) }
    private val primaryColor by lazy { ResourcesCompat.getColor(resources, R.color.colorPrimary, null) }

    @Inject
    lateinit var videoAdapter: VideoPageAdapter
    @Inject
    lateinit var themeManager: ThemeManager

    private val refreshes
        get() = RxSwipeRefreshLayout.refreshes(creator_container).map { CreatorContract.Action.Refresh(creatorId, true) }

    override val actions: Observable<CreatorContract.Action>
        get() = Observable.merge(refreshes, videoAdapter.actions.map(CreatorContract.Action::PlayVideo))
                .startWith(CreatorContract.Action.Refresh(creatorId, false))

    override fun getLayoutId(): Int = R.layout.fragment_creator

    override fun getToolbar(): Toolbar? = creator_toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.creator_list.layoutManager = LinearLayoutManager(requireContext())
        view.creator_list.adapter = videoAdapter
        creator_container_lazy.setupWithSwipeRefreshLayout(creator_container)
        supportActionBar?.title = creatorName
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
                .toPalette().subscribe { paletteEvent ->
                    themeManager.getVibrantSwatch(paletteEvent.palette).apply {
                        animations += AnimatorSet().apply {
                            playTogether(
                                    setStatusBarColor(rgb.darken(.7f)),
                                    ValueAnimator.ofArgb(rgb).apply {
                                        addUpdateListener { creator_toolbar.setBackgroundColor(it.animatedValue as Int) }
                                        animations += this
                                    },
                                    ValueAnimator.ofArgb(titleTextColor).apply {
                                        addUpdateListener {
                                            val value = it.animatedValue as Int
                                            creator_toolbar.setTitleTextColor(value)
                                            creator_toolbar.navigationIcon?.mutate()?.setTint(value)
                                        }
                                        animations += this
                                    }
                            )
                            this.start()
                        }
                    }
                }
    }

    private fun displayVideos(videos: PagedList<Video>) {
        creator_container_lazy.state = LazyLayout.SUCCESS
        videoAdapter.submitList(videos)
    }

    private fun processError(error: CreatorContract.State.Error) {
        creator_container_lazy.errorText = getString(error.type.msg)
        creator_container_lazy.state = LazyLayout.ERROR
    }

    override fun onStop() {
        super.onStop()
        setStatusBarColor(requireActivity().primaryDarkColor).start()
    }

    companion object {
        private const val CreatorIdKey = "CreatorId"
        private const val CreatorNameKey = "CreatorName"
        fun newInstance(creatorId: String, creatorName: String): Fragment = CreatorFragment().apply {
            arguments = bundleOf(CreatorIdKey to creatorId, CreatorNameKey to creatorName)
        }
    }
}