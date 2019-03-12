package me.mauricee.pontoon.main.details.livestream

import androidx.core.os.bundleOf
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_livestream.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout

class LiveStreamFragment : BaseFragment<LiveStreamPresenter>(), LiveStreamContract.View {

    private val creatorId: String
        get() = arguments!!.getString(CreatorKey)!!

    override fun getLayoutId(): Int = R.layout.fragment_livestream

    override val actions: Observable<LiveStreamContract.Action>
        get() = Observable.just(LiveStreamContract.Action.ViewLiveStream(creatorId))

    override fun updateState(state: LiveStreamContract.State) = when (state) {
        LiveStreamContract.State.Loading -> livestream.state = LazyLayout.LOADING
        is LiveStreamContract.State.Error -> {
            livestream.errorText = getString(state.type.message)
            livestream.state = LazyLayout.ERROR
        }
        is LiveStreamContract.State.IsOffline -> {
            livestream.state = LazyLayout.SUCCESS
            livestream_title.text = state.metadata.title
            livestream_subtitle.text = state.metadata.description
        }
        is LiveStreamContract.State.IsOnline -> {
            livestream.state = LazyLayout.SUCCESS
            livestream_title.text = state.metadata.title
            livestream_subtitle.text = state.metadata.description
        }
    }

    companion object {
        private const val CreatorKey = "CreatorKey"
        fun newInstance(creatorId: String) = LiveStreamFragment().apply {
            arguments = bundleOf(CreatorKey to creatorId)
        }
    }
}