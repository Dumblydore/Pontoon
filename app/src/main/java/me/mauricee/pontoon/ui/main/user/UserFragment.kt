package me.mauricee.pontoon.ui.main.user

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.databinding.FragmentUserBinding
import me.mauricee.pontoon.databinding.ItemActivityCommentBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.setStatusBarColor
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.preferences.darken
import me.mauricee.pontoon.rx.glide.toPalette
import me.mauricee.pontoon.ui.NewBaseFragment
import javax.inject.Inject

class UserFragment : NewBaseFragment(R.layout.fragment_user) {


    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var factory: UserViewModel.Factory

    private val viewModel: UserViewModel by viewModels { factory }
    private val binding: FragmentUserBinding by viewBinding(FragmentUserBinding::bind)

    private val adapter = SimpleBindingAdapter(ItemActivityCommentBinding::inflate, ::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userContainerActivity.adapter = adapter

        subscriptions += adapter.clicks.map { UserAction.ActivityClicked(it.model) }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.userContainer.refreshes().map { UserAction.Refresh }
                .subscribe(viewModel::sendAction)

        viewModel.state.mapDistinct { it.uiState.lazyState() }.observe(viewLifecycleOwner) {
            binding.userContainerLazy.state = it
        }
        viewModel.state.map { it.uiState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.userContainer.isRefreshing = it
        }
        viewModel.state.mapDistinct(UserState::activity).observe(viewLifecycleOwner, adapter::submitList)
        viewModel.state.mapDistinct(UserState::user).notNull().observe(viewLifecycleOwner, ::displayUser)
    }

    private fun displayUser(user: UserEntity) {
        subscriptions += GlideApp.with(this).asBitmap().load(user.profileImage).toPalette().subscribe { paletteEvent ->
            themeManager.getVibrantSwatch(paletteEvent.palette).apply {
                AnimatorSet().apply {
                    playTogether(
                            setStatusBarColor(rgb.darken(.7f)),
                            ValueAnimator.ofArgb(rgb).apply { addUpdateListener { binding.userToolbar.setBackgroundColor(it.animatedValue as Int) } },
                            ValueAnimator.ofArgb(rgb.darken(.5f)).apply { addUpdateListener { binding.userContainerHeader.setBackgroundColor(it.animatedValue as Int) } },
                            ValueAnimator.ofArgb(titleTextColor).apply {
                                addUpdateListener {
                                    val value = it.animatedValue as Int
                                    binding.userToolbar.setTitleTextColor(value)
                                    binding.userToolbar.navigationIcon?.mutate()?.setTint(value)
                                }
                            }
                    )
                }.start()
            }
            GlideApp.with(this)
                    .load(paletteEvent.bitmap)
                    .circleCrop().into(binding.userContainerUserIcon)
        }
    }

    private fun bind(view: ItemActivityCommentBinding, activity: ActivityEntity) {
        view.itemTitle.text = getString(R.string.activity_comment_context, activity.postId)
        view.itemComment.text = activity.comment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setStatusBarColor(requireActivity().primaryDarkColor).start()
    }

    companion object {
        internal const val UserKey = "UserKey"
        fun newInstance(userId: String): Fragment = UserFragment().apply { arguments = bundleOf(UserKey to userId) }
    }
}