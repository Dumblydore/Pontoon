package me.mauricee.pontoon.ui.main.user

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.item_activity_comment.view.*
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.LazyLayout
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.common.SpaceItemDecoration
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.ext.setStatusBarColor
import me.mauricee.pontoon.ext.supportActionBar
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.preferences.darken
import me.mauricee.pontoon.rx.glide.toPalette
import javax.inject.Inject

class UserFragment : UserContract.View, BaseFragment<UserPresenter>() {


    @Inject
    lateinit var themeManager: ThemeManager

    private val adapter = SimpleListAdapter(R.layout.item_activity_comment, ::bind)

    override val actions: Observable<UserContract.Action>
        get() = Observable.merge(adapter.clicks.map { UserContract.Action.Video(it.model.postId) },
                RxSwipeRefreshLayout.refreshes(user_container).map { refresh })
                .startWith(refresh)

    private val refresh by lazy { UserContract.Action.Refresh(requireArguments().getString(UserKey)!!) }

    override fun getLayoutId(): Int = R.layout.fragment_user

    override fun getToolbar(): Toolbar? = user_toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user_container_lazy.setupWithSwipeRefreshLayout(user_container)
        user_container_activity.layoutManager = LinearLayoutManager(requireContext())
        user_container_activity.adapter = adapter
        user_container_activity.addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing)))

    }

    override fun updateState(state: UserContract.State) = when (state) {
        UserContract.State.Loading -> user_container_lazy.state = LazyLayout.LOADING
        is UserContract.State.DisplayUser -> updateUser(state.user.entity, state.user.activities)
        is UserContract.State.Error -> user_container_lazy.state = LazyLayout.ERROR
    }

    private fun updateUser(user: UserEntity, activities: List<ActivityEntity>) {
        adapter.submitList(activities)
        user_container_lazy.state = LazyLayout.SUCCESS
        supportActionBar?.title = user.username
        subscriptions += GlideApp.with(this).asBitmap().load(user.profileImage).toPalette().subscribe { paletteEvent ->
            themeManager.getVibrantSwatch(paletteEvent.palette).apply {
                AnimatorSet().apply {
                    playTogether(
                            setStatusBarColor(rgb.darken(.7f)),
                            ValueAnimator.ofArgb(rgb).apply { addUpdateListener { user_toolbar.setBackgroundColor(it.animatedValue as Int) } },
                            ValueAnimator.ofArgb(rgb.darken(.5f)).apply { addUpdateListener { user_container_header.setBackgroundColor(it.animatedValue as Int) } },
                            ValueAnimator.ofArgb(titleTextColor).apply {
                                addUpdateListener {
                                    val value = it.animatedValue as Int
                                    user_toolbar.setTitleTextColor(value)
                                    user_toolbar.navigationIcon?.mutate()?.setTint(value)
                                }
                            }
                    )
                }.start()
            }
            GlideApp.with(user_container_userIcon)
                    .load(paletteEvent.bitmap)
                    .circleCrop().into(user_container_userIcon)
        }
    }

    private fun bind(view: View, activity: ActivityEntity) {
        view.item_title.text = getString(R.string.activity_comment_context, activity.postId)
        view.item_comment.text = activity.comment
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