package me.mauricee.pontoon.ui.main.player

import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.google.android.gms.cast.framework.CastButtonFactory
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.touches
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentPlayerBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.ui.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : BaseFragment(R.layout.fragment_player), MotionLayout.TransitionListener {

    @Inject
    lateinit var newPlayer: Player
    private val viewModel: PlayerViewModel by viewModels({ requireActivity() })
    private val binding by viewBinding(FragmentPlayerBinding::bind)

    private val qualityAdapter by lazy { ArrayAdapter<Player.Quality>(requireContext(), R.layout.item_popup) }
    private val popupWindow by lazy { ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply { setAdapter(qualityAdapter) } }
    private var startTime = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newPlayer.bindToTexture(binding.playerSurface)

        CastButtonFactory.setUpMediaRouteButton(requireContext(), binding.playerControlCast)

        binding.root.addTransitionListener(this)
        popupWindow.anchorView = binding.playerControlQuality

        popupWindow.setOnItemClickListener { _, _, position, _ ->
            qualityAdapter.getItem(position)?.let {
                viewModel.sendAction(PlayerAction.SetQuality(it))
            }
        }
        subscriptions += binding.playerControlsExpand.clicks()
                .map { PlayerAction.SetViewMode(ViewMode.Collapsed) }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.playerControlsFullscreen.clicks()
                .map { PlayerAction.ToggleFullscreen }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.playerControlPlayPause.clicks()
                .map { PlayerAction.TogglePlayPause }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.playerControlQuality.clicks().subscribe { popupWindow.show() }

        viewModel.state.mapDistinct(PlayerState::currentQualityLevel).observe(viewLifecycleOwner) {
            binding.playerControlQuality.apply {
                isGone = it == null
                text = it?.label
            }
            popupWindow.setSelection(qualityAdapter.getPosition(it))
        }
        viewModel.state.mapDistinct { it.qualityLevels.toList() }.observe(viewLifecycleOwner) {
            qualityAdapter.apply {
                clear()
                addAll(it)
                notifyDataSetInvalidated()
            }
        }
        viewModel.state.mapDistinct(PlayerState::isBuffering).notNull().observe(viewLifecycleOwner) {
            if (it) binding.playerControlBuffering.show()
            else binding.playerControlBuffering.hide()
        }
        viewModel.state.mapDistinct(PlayerState::controlsVisible).notNull().observe(viewLifecycleOwner) {
            if (it) binding.root.transitionToStart()
            else binding.root.transitionToEnd()
        }
        viewModel.state.mapDistinct { it.isPlaying }.notNull()
                .map { if (it) R.drawable.ic_pause else R.drawable.ic_play }
                .observe(viewLifecycleOwner, binding.playerControlPlayPause::setIconResource)

        viewModel.state.mapDistinct(PlayerState::timestamp).notNull().observe(viewLifecycleOwner) {
            binding.playerControlsTimestamp.text = it
        }
        viewModel.state.mapDistinct(PlayerState::previewImage).notNull().observe(viewLifecycleOwner) {
            Glide.with(this).load(it).into(binding.playerPreview)
        }
        viewModel.state.mapDistinct(PlayerState::viewMode).observe(viewLifecycleOwner) {
            when (it) {
                ViewMode.Expanded -> {
                    binding.controlsGroup.isGone = false
                    binding.controlsGroup.doOnNextLayout { binding.root.transitionToStart() }
                    binding.playerControlsExpand.isGone = false
                    binding.playerControlsFullscreen.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fullscreen)
                }
                ViewMode.Fullscreen -> {
                    binding.controlsGroup.isGone = false
                    binding.controlsGroup.doOnNextLayout { binding.root.transitionToStart() }
                    binding.playerControlsExpand.isGone = true
                    binding.playerControlsFullscreen.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fullscreen_exit)
                }
                ViewMode.Dismissed -> binding.root.transitionToEnd()
                ViewMode.Collapsed -> binding.root.transitionToEnd()
                ViewMode.PictureInPicture -> binding.root.transitionToEnd()
            }
        }
    }

    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
    }

    override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

    override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
    }

    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
}