package me.mauricee.pontoon.ui.main.user

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.SimpleBindingAdapter
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.common.theme.primaryDarkColor
import me.mauricee.pontoon.databinding.FragmentUserBinding
import me.mauricee.pontoon.databinding.ItemActivityCommentBinding
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import me.mauricee.pontoon.rx.glide.toPalette
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.assistedViewModel
import me.mauricee.pontoon.ui.preferences.darken
import javax.inject.Inject

@AndroidEntryPoint
class UserFragment : BaseFragment(R.layout.fragment_user) {


    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var presenterFactory: UserPresenter.Factory

    @Inject
    lateinit var viewModelFactory: UserViewModel.Factory

    private val args by navArgs<UserFragmentArgs>()

    private val viewModel: UserViewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(UserArgs(args.userId)))
    }
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

        viewModel.state.mapDistinct { it.user?.username }.notNull().observe(viewLifecycleOwner) {
            binding.userContainerSubtitle.text = getString(R.string.user_container_subtitle, it)
        }
        viewModel.state.mapDistinct { it.user?.profileImage }.notNull().observe(viewLifecycleOwner) { url ->
            subscriptions += Glide.with(this).asBitmap().load(url).toPalette()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { event ->
                        themeManager.getVibrantSwatch(event.palette)?.let { setUserTheme(event.bitmap, it) }
                        GlideApp.with(this).load(event.bitmap).circleCrop().into(binding.userContainerUserIcon)
                    }
        }
    }

    private fun setUserTheme(icon: Bitmap, swatch: Palette.Swatch) {
        animations += AnimatorSet().apply {
            playTogether(requireActivity().animateStatusBarColor(swatch.rgb.darken(.7f)),
                    ValueAnimator.ofArgb(swatch.rgb).updateAsInt(binding.userToolbar::setBackgroundColor),
                    ValueAnimator.ofArgb(swatch.rgb.darken(.5f)).updateAsInt(binding.userContainerHeader::setBackgroundColor),
                    ValueAnimator.ofArgb(swatch.titleTextColor).updateAsInt {
                        binding.userToolbar.setTitleTextColor(it)
                        binding.userToolbar.navigationIcon?.mutate()?.setTint(it)
                    })
            start()
        }
    }

    private fun bind(view: ItemActivityCommentBinding, activity: ActivityEntity) {
        view.itemTitle.text = getString(R.string.activity_comment_context)
        view.itemComment.text = activity.comment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setStatusBarColor(requireActivity().primaryDarkColor).start()
    }
}