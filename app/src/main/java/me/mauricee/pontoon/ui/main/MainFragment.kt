package me.mauricee.pontoon.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.jakewharton.rxbinding2.support.design.widget.itemSelections
import com.jakewharton.rxbinding2.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.SessionGraphDirections.*
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.FragmentMainBinding
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.MainFragmentDirections.actionMainFragmentToAboutFragment2
import me.mauricee.pontoon.ui.main.MainFragmentDirections.actionMainFragmentToLoginGraph
import me.mauricee.pontoon.ui.main.player.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : NewBaseFragment(R.layout.fragment_main), MotionLayout.TransitionListener {

    @Inject
    lateinit var player: NewPlayer

    @Inject
    lateinit var orientationManager: OrientationManager

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: MainContract.ViewModel by viewModels({ requireActivity() })
    private val playerViewModel: PlayerViewModel by viewModels({ requireActivity() })

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val childNavController: NavController
        get() = (childFragmentManager.findFragmentById(binding.mainContainer.id) as NavHostFragment).navController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NavigationUI.setupWithNavController(binding.mainNav, childNavController)
        binding.main.addTransitionListener(this)

        subscriptions += binding.collapsedDetailsPlayPause.clicks()
                .flatMapCompletable { player.togglePlayPause() }
                .subscribe()

        subscriptions += binding.mainDrawer.itemSelections()
                .map { MainContract.Action.fromNavDrawer(it.itemId) }
                .subscribe(viewModel::sendAction)

        viewModel.events.observe(viewLifecycleOwner, ::handleEvents)
        playerViewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                is PlayerEvent.DisplayUser -> {
                    childNavController.navigate(actionGlobalUserFragment(it.user.id))
                    playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.Collapsed))
                }
                is PlayerEvent.DisplayCreator -> {
                    childNavController.navigate(actionGlobalCreatorFragment(it.creator.id))
                    playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.Collapsed))
                }
                is PlayerEvent.PostComment -> childNavController.navigate(actionGlobalCommentDialogFragment(it.videoId, it.comment))
                is PlayerEvent.DisplayReplies -> childNavController.navigate(actionGlobalRepliesDialogFragment(it.commentId))
            }
        }

        playerViewModel.state.map(PlayerState::viewMode).observe(viewLifecycleOwner, ::handlePlayerViewMode)
        playerViewModel.state.mapDistinct { it.video?.entity?.title }.notNull().observe(viewLifecycleOwner) {
            binding.collapsedDetailsTitle.text = it
        }
        playerViewModel.state.mapDistinct { it.video?.creator?.entity?.name }.notNull().observe(viewLifecycleOwner) {
            binding.collapsedDetailsSubtitle.text = it
        }
    }

    private fun handlePlayerViewMode(it: ViewMode) {
        when (it) {
            ViewMode.Dismissed -> {
                setWindowVisibility(true)
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = false
            }
            ViewMode.Expanded -> {
                copyContentForSlideGesture()
                binding.mainContainerPreview.isGone = false
                binding.mainContainer.isGone = true
                orientationManager.isFullscreen = false
                setWindowVisibility(true)
                playVideo()
            }
            ViewMode.Fullscreen -> {
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = true
                orientationManager.isFullscreen = true
                setWindowVisibility(false)
                binding.main.transitionToState(R.id.fullscreen)
            }
            ViewMode.Collapsed -> {
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = false
                binding.main.setTransition(R.id.transition_active)
                binding.main.transitionToEnd()
            }
        }
    }

    private fun copyContentForSlideGesture() {
        if (binding.mainContainerPreview.isGone && !orientationManager.isFullscreen) {
            val w = binding.root.measuredWidth
            val h = binding.root.measuredHeight
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)?.let {
                val canvas = Canvas(it)
                binding.mainContainer.draw(canvas)
                binding.mainContainerPreview.setImageBitmap(it)
            }
        }
    }

    private fun handleEvents(event: MainContract.Event) {
        when (event) {
            is MainContract.Event.TriggerNightMode -> themeManager.toggleNightMode()
            is MainContract.Event.NavigateToUser -> childNavController.navigate(actionGlobalUserFragment(event.user.id))
            MainContract.Event.ToggleMenu -> toggleDrawer()
            MainContract.Event.NavigateToPreferences -> findNavController().navigate(actionMainFragmentToAboutFragment2())
            MainContract.Event.NavigateToLoginScreen -> findNavController().navigate(actionMainFragmentToLoginGraph())
            MainContract.Event.SessionExpired -> TODO()
            MainContract.Event.CloseMenu -> binding.root.closeDrawer(binding.mainDrawer)
        }
    }

    private fun toggleDrawer() {
        if (binding.root.isDrawerOpen(binding.mainDrawer)) {
            binding.root.closeDrawer(binding.mainDrawer)
        } else {
            binding.root.openDrawer(binding.mainDrawer)
        }
    }

    private fun handleExpanded(isExpanded: Boolean) {
        if (isExpanded) {
            binding.main.setTransition(R.id.transition_active)
        }
    }

    private fun playVideo() {
        when (binding.main.currentState) {
            R.id.fullscreen -> {
                binding.main.transitionToState(R.id.expanded)
                binding.main.setTransition(R.id.transition_active)
            }
            R.id.collapsed -> {
                binding.main.setTransition(R.id.transition_active)
                binding.main.transitionToStart()
            }
            R.id.dismissed -> {
                binding.main.setTransition(R.id.transition_inactive)
                binding.main.transitionToEnd()
            }
        }

    }

    private fun setWindowVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    override fun onTransitionStarted(p0: MotionLayout, p1: Int, p2: Int) {
        logd("On Transition started")
    }

    override fun onTransitionChange(p0: MotionLayout, p1: Int, p2: Int, p3: Float) {}

    override fun onTransitionCompleted(p0: MotionLayout, p1: Int) {
        logd("On Transition completed")
        val currentState = p0.currentState
        handleExpanded(p0.currentState == R.id.expanded)

        val newViewMode = when (currentState) {
            R.id.fullscreen -> ViewMode.Fullscreen
            R.id.expanded -> ViewMode.Expanded
            R.id.collapsed -> ViewMode.Collapsed
            else -> ViewMode.Dismissed
        }
        playerViewModel.sendAction(PlayerAction.SetViewMode(newViewMode))
    }

    override fun onTransitionTrigger(p0: MotionLayout, p1: Int, p2: Boolean, p3: Float) {}
}