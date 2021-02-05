package me.mauricee.pontoon.ui.main.player

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.SeekBarProgressChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding2.widget.SeekBarStopChangeEvent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentPlayerBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.playback.Player
import me.mauricee.pontoon.rx.glide.toSingle
import me.mauricee.pontoon.ui.BaseFragment
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : BaseFragment(R.layout.fragment_player) {

    @Inject
    lateinit var newPlayer: Player
    private val viewModel: PlayerViewModel by viewModels({ requireActivity() })
    private val binding by viewBinding(FragmentPlayerBinding::bind)

    private var isSeeking: Boolean = false
    private var pendingSeek: Long = 0L
    private lateinit var mediaRouteMenuItem: MenuItem

    private val qualityAdapter by lazy { ArrayAdapter<Player.Quality>(requireContext(), R.layout.item_popup) }
    private val popupWindow by lazy { ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle).apply { setAdapter(qualityAdapter) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newPlayer.bindToTexture(binding.playerSurface)

        popupWindow.anchorView = binding.playerControlQuality

        popupWindow.setOnItemClickListener { _, _, position, _ ->
            qualityAdapter.getItem(position)?.let {
                viewModel.sendAction(PlayerAction.SetQuality(it))
            }
        }
        subscriptions += binding.playerControlsFullscreen.clicks()
                .map { PlayerAction.ToggleFullscreen }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.playerControlPlayPause.clicks()
                .map { PlayerAction.TogglePlayPause }
                .subscribe(viewModel::sendAction)
        subscriptions += binding.playerControlQuality.clicks().subscribe { popupWindow.show() }
        subscriptions += binding.playerProgress.seekBarChanges.sample(125, TimeUnit.MILLISECONDS)
                .filter { it is SeekBarProgressChangeEvent }
                .cast(SeekBarProgressChangeEvent::class.java)
                .filter(SeekBarProgressChangeEvent::fromUser)
                .map { it.progress() * 1000L }
                .subscribe { pendingSeek = it }
        subscriptions += binding.playerProgress.seekBarChanges.subscribe {
            when (it) {
                is SeekBarStartChangeEvent -> isSeeking = true
                is SeekBarStopChangeEvent -> {
                    isSeeking = false
                    viewModel.sendAction(PlayerAction.SeekTo(pendingSeek))
                }

            }
        }
        viewModel.state.mapDistinct(PlayerState::duration).observe(viewLifecycleOwner) {
            if (!isSeeking)
                binding.playerProgress.duration = it
        }
        viewModel.state.mapDistinct(PlayerState::position).observe(viewLifecycleOwner) {
            if (!isSeeking)
                binding.playerProgress.progress = it
        }
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
        viewModel.state.mapDistinct { it.isPlaying }.notNull()
                .map { if (it) R.drawable.ic_pause else R.drawable.ic_play }
                .observe(viewLifecycleOwner, binding.playerControlPlayPause::setIconResource)
        viewModel.state.mapDistinct(PlayerState::timelineUrl).notNull().observe(viewLifecycleOwner) { url ->
            subscriptions += GlideApp.with(this).asBitmap().load(url)
                    .toSingle().subscribe({ binding.playerProgress.timelineBitmap = it },
                            { binding.playerProgress.timelineBitmap = null })
        }
        viewModel.state.mapDistinct(PlayerState::timestamp).notNull().observe(viewLifecycleOwner) {
            binding.playerControlsTimestamp.text = it
        }
        viewModel.state.mapDistinct(PlayerState::viewMode).observe(viewLifecycleOwner) {
            when (it) {
                ViewMode.Expanded -> binding.root.transitionToStart()
                ViewMode.Fullscreen -> binding.root.transitionToStart()
                ViewMode.Dismissed -> binding.root.transitionToEnd()
                ViewMode.Collapsed -> binding.root.transitionToEnd()
                ViewMode.PictureInPicture -> binding.root.transitionToEnd()
            }
        }
    }
}