package me.mauricee.pontoon.main.user

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_user.*
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.SpaceItemDecoration
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.ext.setStatusBarColor
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.preferences.darken
import me.mauricee.pontoon.rx.glide.toPalette
import javax.inject.Inject

class UserFragment : UserContract.View, BaseFragment<UserPresenter>() {

    @Inject
    lateinit var adapter: UserActivityAdapter
    @Inject
    lateinit var themeManager: ThemeManager

    override val actions: Observable<UserContract.Action>
        get() = Observable.merge(adapter.actions,
                RxSwipeRefreshLayout.refreshes(user_container).map { refresh })
                .startWith(refresh)

    private val refresh by lazy { UserContract.Action.Refresh(arguments!!.getString(UserKey)) }
    override fun getLayoutId(): Int = R.layout.fragment_user

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user_container_lazy.setupWithSwipeRefreshLayout(user_container)
        user_container_activity.layoutManager = LinearLayoutManager(requireContext())
        user_container_activity.adapter = adapter
        user_container_activity.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))
    }

    override fun updateState(state: UserContract.State) = when (state) {
        is UserContract.State.User -> {
            user_toolbar.title = state.user.username
            subscriptions += GlideApp.with(this).asBitmap().load(state.user.profileImage).toPalette().subscribe { paletteEvent ->
                themeManager.getVibrantSwatch(paletteEvent.palette).apply {
                    AnimatorSet().apply {
                        playTogether(
                                setStatusBarColor(rgb.darken(.7f)),
                                ValueAnimator.ofArgb(rgb).apply { addUpdateListener { user_toolbar.setBackgroundColor(it.animatedValue as Int) } },
                                ValueAnimator.ofArgb(rgb.darken(.5f)).apply { addUpdateListener { user_container_header.setBackgroundColor(it.animatedValue as Int) } },
                                ValueAnimator.ofArgb(titleTextColor).apply { addUpdateListener { user_toolbar.setTitleTextColor(it.animatedValue as Int) } }
                        )
                    }.start()
                }
                GlideApp.with(user_container_userIcon)
                        .load(paletteEvent.bitmap)
                        .circleCrop().into(user_container_userIcon)
            }
        }
        UserContract.State.Loading -> user_container_lazy.state = LazyLayout.LOADING
        is UserContract.State.Activity -> {
            adapter.submitList(state.activity)
            user_container_lazy.state = LazyLayout.SUCCESS
        }
        is UserContract.State.Error -> user_container_lazy.state = LazyLayout.ERROR
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setStatusBarColor(requireActivity().primaryDarkColor).start()
    }

    companion object {
        private const val UserKey = "UserKey"
        fun newInstance(userId: String): Fragment = UserFragment().apply { arguments = bundleOf(UserKey to userId) }
    }
}