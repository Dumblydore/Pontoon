package me.mauricee.pontoon.ui.main.player.playback

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.layout_player_controls.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentPlayerBinding
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.player.PlayerAction
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.player.ViewMode
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : NewBaseFragment(R.layout.fragment_player) {

    @Inject
    lateinit var newPlayer: NewPlayer
    private val viewModel: PlayerViewModel by viewModels({ requireActivity() })
    private val binding by viewBinding(FragmentPlayerBinding::bind)


    private val qualityMenu by lazy { player_controls_toolbar.menu.findItem(R.id.action_stream) }
    private var isSeeking: Boolean = false
    private lateinit var mediaRouteMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newPlayer.bindToPlayer(binding.playerDisplay)
        subscriptions += binding.playerDisplay.fullscreenClicked().map {
            PlayerAction.SetViewMode(ViewMode.Fullscreen)
        }.subscribe(viewModel::sendAction)
        viewModel.watchStateValue { viewMode }.observe(viewLifecycleOwner) {
//            when (it) {
//                is ViewMode.FullScreen -> {
//                    if (it.controlsEnabled) view.player_display.showController() else view.player_display.hideController()
//                }
//                is ViewMode.Expanded -> {
//                    if (it.controlsEnabled) view.player_display.showController() else view.player_display.hideController()
//                }
//                else -> view.player_display.hideController()
//            }
        }
    }
}