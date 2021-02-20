package me.mauricee.pontoon.ui.main.creator

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.FragmentCreatorBinding
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.rx.glide.PaletteSingle
import me.mauricee.pontoon.rx.glide.toPalette
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.assistedViewModel
import me.mauricee.pontoon.ui.main.VideoPageAdapter
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.preferences.darken
import me.mauricee.pontoon.ui.shareVideo
import javax.inject.Inject

@AndroidEntryPoint
class CreatorFragment : BaseFragment(R.layout.fragment_creator) {

    @Inject
    lateinit var videoAdapter: VideoPageAdapter

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var presenterFactory: CreatorPresenter.Factory

    @Inject
    lateinit var viewModelFactory: CreatorContract.ViewModel.Factory

    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val viewModel by assistedViewModel {
        viewModelFactory.create(presenterFactory.create(CreatorContract.Args(args.creatorId)))
    }

    private val binding by viewBinding(FragmentCreatorBinding::bind)
    private val args by navArgs<CreatorFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changingStatusBarColor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NavigationUI.setupWithNavController(binding.creatorToolbar, findNavController())

        binding.creatorList.adapter = videoAdapter

        viewModel.state.mapDistinct { it.creator?.entity?.name }.notNull().observe(viewLifecycleOwner) {
            binding.creatorToolbar.title = it
        }
        viewModel.state.mapDistinct { it.creator?.user?.profileImage }.notNull().observe(viewLifecycleOwner) {
            subscriptions += Glide.with(this).asBitmap().load(it).toPalette()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(PaletteSingle.PaletteEvent::palette)
                    .subscribe(::setCreatorTheme)
        }

        viewModel.state.mapDistinct { it.screenState.lazyState() }
                .observe(viewLifecycleOwner) { binding.creatorContainerLazy.state = it }
        viewModel.state.map { it.screenState.isRefreshing() }.observe(viewLifecycleOwner) {
            binding.creatorContainer.isRefreshing = it
        }
        viewModel.state.mapDistinct { it.screenState.error }.notNull().observe(viewLifecycleOwner) {
            binding.creatorContainerLazy.errorText = it.text(requireContext())
        }
        viewModel.state.mapDistinct { it.videos }.observe(viewLifecycleOwner, videoAdapter::submitList)

        subscriptions += binding.creatorContainer.refreshes().subscribe {
            viewModel.sendAction(CreatorContract.Action.Refresh)
        }
        subscriptions += videoAdapter.actions.subscribe {
            playerViewModel.sendAction(PlayerAction.PlayVideo(it.id))
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        videoAdapter.contextVideo?.let { video: Video ->
            when (item.itemId) {
                R.id.action_share -> requireActivity().shareVideo(video)
                else -> null
            }
        }
        return true
    }

    private fun setCreatorTheme(palette: Palette) {
        themeManager.getVibrantSwatch(palette)?.apply {
            animations += AnimatorSet().apply {
                playTogether(requireActivity().animateStatusBarColor(rgb.darken(.7f)),
                        ValueAnimator.ofArgb(rgb).updateAsInt(binding.creatorToolbar::setBackgroundColor),
                        ValueAnimator.ofArgb(titleTextColor).updateAsInt {
                            binding.creatorToolbar.setTitleTextColor(it)
                            binding.creatorToolbar.navigationIcon?.mutate()?.setTint(it)
                        }
                )
                start()
            }
        }
    }
}