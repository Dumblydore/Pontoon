package me.mauricee.pontoon.ui.main

import android.animation.ValueAnimator
import android.app.PictureInPictureParams
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.animation.doOnStart
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.jakewharton.rxbinding3.material.itemSelections
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.layoutChanges
import com.jakewharton.rxbinding3.widget.SeekBarProgressChangeEvent
import com.jakewharton.rxbinding3.widget.SeekBarStartChangeEvent
import com.jakewharton.rxbinding3.widget.SeekBarStopChangeEvent
import com.jakewharton.rxbinding3.widget.changeEvents
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.SessionGraphDirections.*
import me.mauricee.pontoon.common.log.logd
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.FragmentMainBinding
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.ui.BaseFragment
import me.mauricee.pontoon.ui.main.MainFragmentDirections.actionMainFragmentToLoginGraph
import me.mauricee.pontoon.ui.main.MainFragmentDirections.actionMainFragmentToSettingsFragment
import me.mauricee.pontoon.ui.main.player.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main), MotionLayout.TransitionListener {

    @Inject
    lateinit var orientationManager: OrientationManager

    @Inject
    lateinit var themeManager: ThemeManager

    private val rect = Rect()
    private val viewModel: MainContract.ViewModel by viewModels({ requireActivity() })
    private val playerViewModel: PlayerViewModel by viewModels({ requireActivity() })

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val childNavController: NavController
        get() = (childFragmentManager.findFragmentById(binding.mainContainer.id) as NavHostFragment).navController

    private var isSeeking: Boolean = false
    private var pendingSeek: Long = 0L
    private var currentThumbAnimation: ValueAnimator? = null
        set(value) {
            field?.cancel()
            field = value?.also { animations += it }
        }
    private var oldStatusBarColor = Color.BLACK
    private lateinit var windowController: WindowInsetsControllerCompat


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logd("test: dismissed=${R.id.dismissed} | expanded=${R.id.expanded} | fullscreen=${R.id.fullscreen} | collapsed=${R.id.collapsed} |dismissing=${R.id.dismissing}")

        NavigationUI.setupWithNavController(binding.mainNav, childNavController)
        windowController = WindowInsetsControllerCompat(requireActivity().window, view)
        binding.main.addTransitionListener(this)

        subscriptions += binding.collapsedDetailsPlayPause.clicks()
            .map { PlayerAction.TogglePlayPause }
            .subscribe(playerViewModel::sendAction)

        subscriptions += binding.mainDrawer.itemSelections()
            .map { MainContract.Action.fromNavDrawer(it.itemId) }
            .subscribe(viewModel::sendAction)

        subscriptions += binding.playerProgress.changeEvents().skipInitialValue().subscribe {
            when (it) {
                is SeekBarStartChangeEvent -> isSeeking = true
                is SeekBarProgressChangeEvent -> {
                    if (it.fromUser) {
                        binding.expandedPreview.progress = it.progress
                        pendingSeek = it.progress * 1000L
                    }
                }
                is SeekBarStopChangeEvent -> {
                    isSeeking = false
                    binding.expandedPreview.hide()
                    playerViewModel.sendAction(PlayerAction.SeekTo(pendingSeek))
                }
            }
        }
        subscriptions += binding.player.layoutChanges()
            .subscribe {
                binding.player.getGlobalVisibleRect(rect)
                requireActivity().setPictureInPictureParams(
                    PictureInPictureParams.Builder()
                        .setSourceRectHint(rect)
                        .build()
                )
            }
        subscriptions += binding.main.playerClicks.map { PlayerAction.ToggleControls }
            .subscribe(playerViewModel::sendAction)

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
                is PlayerEvent.PostComment -> childNavController.navigate(
                    actionGlobalCommentDialogFragment(it.videoId, it.comment)
                )
                is PlayerEvent.DisplayReplies -> childNavController.navigate(
                    actionGlobalRepliesDialogFragment(it.commentId)
                )
                PlayerEvent.RunInBackground -> runInBackground()
                else -> logd("Unhandled: ${it::class.java.simpleName}")
            }
        }
        playerViewModel.state.mapDistinct(PlayerState::controlsVisible).notNull()
            .observe(viewLifecycleOwner) {
                currentThumbAnimation = if (binding.main.currentState == R.id.fullscreen)
                    animateProgress(if (it) 1f else 0f)
                else
                    animateThumb(if (it) 255 else 0)
                currentThumbAnimation?.start()
                binding.main.allowPlayerClick = it
            }
        playerViewModel.state.mapDistinct(PlayerState::duration).map { (it / 1000).toInt() }
            .observe(viewLifecycleOwner) {
                binding.playerProgress.max = it
                binding.collapsedProgress.max = it
                binding.expandedPreview.duration = it
            }
        playerViewModel.state.mapDistinct(PlayerState::position).map { (it / 1000).toInt() }
            .observe(viewLifecycleOwner) {
                binding.playerProgress.progress = it
                binding.collapsedProgress.progress = it
            }
        playerViewModel.state.mapDistinct(PlayerState::viewMode)
            .observe(viewLifecycleOwner, ::handlePlayerViewMode)
        playerViewModel.state.mapDistinct { it.video?.title }.notNull()
            .observe(viewLifecycleOwner) {
                binding.collapsedDetailsTitle.text = it
            }
        playerViewModel.state.mapDistinct { it.video?.creator?.name }.notNull()
            .observe(viewLifecycleOwner) {
                binding.collapsedDetailsSubtitle.text = it
            }
        playerViewModel.state.mapDistinct { it.isPlaying }.notNull()
            .map { if (it) R.drawable.ic_pause else R.drawable.ic_play }
            .observe(viewLifecycleOwner, binding.collapsedDetailsPlayPause::setIconResource)
    }

    private fun runInBackground() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags += Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        val action = if (isInPictureInPictureMode) {
            PlayerAction.SetViewMode(ViewMode.PictureInPicture)
        } else {
            PlayerAction.SetViewMode(ViewMode.Expanded)
        }
        playerViewModel.sendAction(action)
    }

    private fun handlePlayerViewMode(it: ViewMode) {
        val currentMode = when (binding.main.currentState) {
            R.id.expanded -> ViewMode.Expanded
            R.id.fullscreen -> ViewMode.Fullscreen
            R.id.collapsed -> ViewMode.Collapsed
            R.id.dismissed,
            R.id.dismissing -> ViewMode.Dismissed
            else -> null
        }
        logd("currentMode: $currentMode , newMode: $it")
        when (it) {
            ViewMode.Dismissed -> {
                setWindowVisibility(true)
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = false
                binding.playerProgress.isGone = false
            }
            ViewMode.Expanded -> {
                copyContentForSlideGesture()
                oldStatusBarColor = requireActivity().statusBarColor
                animations += requireActivity().animateStatusBarColor(Color.BLACK).apply { start() }
                binding.mainContainerPreview.isGone = false
                binding.mainContainer.isGone = true
                orientationManager.isFullscreen = false
                binding.playerProgress.isGone = false
                setWindowVisibility(true)
                playVideo()
            }
            ViewMode.Fullscreen -> {
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = true
                orientationManager.isFullscreen = true
                binding.playerProgress.isGone = false
                setWindowVisibility(false)
                binding.main.transitionToState(R.id.fullscreen)
            }
            ViewMode.Collapsed -> {
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = false
                binding.playerProgress.isGone = false
                if (currentMode == ViewMode.Dismissed) {
                    binding.main.setTransition(R.id.transition_active_collapse)
                    binding.main.transitionToEnd()
                } else {
                    binding.main.setTransition(R.id.transition_active)
                    binding.main.transitionToEnd()
                    animations += requireActivity().animateStatusBarColor(oldStatusBarColor)
                        .apply { start() }
                }
            }
            ViewMode.PictureInPicture -> {
                binding.mainContainerPreview.isGone = true
                binding.mainContainer.isGone = true
                binding.playerProgress.isGone = true
                setWindowVisibility(false)
                binding.main.transitionToState(R.id.fullscreen)
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
            is MainContract.Event.NavigateToUser -> childNavController.navigate(
                actionGlobalUserFragment(event.user.id)
            )
            MainContract.Event.ToggleMenu -> toggleDrawer()
            MainContract.Event.NavigateToPreferences -> findNavController().navigate(
                actionMainFragmentToSettingsFragment()
            )
            MainContract.Event.NavigateToLoginScreen -> findNavController().navigate(
                actionMainFragmentToLoginGraph()
            )
            MainContract.Event.CloseMenu -> binding.root.closeDrawer(binding.mainDrawer)
            MainContract.Event.SessionExpired -> onSessionExpired()
        }
    }

    private fun onSessionExpired() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.main_session_expired_title)
            .setMessage(R.string.main_session_expired_body)
            .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.sendAction(MainContract.Action.Expired) }
            .setCancelable(false)
            .create().show()
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
            windowController.show(WindowInsetsCompat.Type.systemBars())
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            windowController.hide(WindowInsetsCompat.Type.systemBars())
            windowController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    private fun animateProgress(to: Float): ValueAnimator {
        return ValueAnimator.ofFloat(binding.playerProgress.alpha, to).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                binding.playerProgress.alpha = value
            }
            doOnStart { binding.playerProgress.thumb.mutate().alpha = 255 }
        }
    }

    private fun animateThumb(to: Int): ValueAnimator {
        return ValueAnimator.ofInt(binding.playerProgress.thumb.alpha, to).apply {
            addUpdateListener {
                val thumb = binding.playerProgress.thumb.mutate()
                val value = it.animatedValue as Int
                thumb.alpha = value
                doOnStart {
                    binding.playerProgress.apply {
                        isGone = false
                        alpha = 1f
                    }
                }
            }
        }
    }

    override fun onTransitionStarted(p0: MotionLayout, startId: Int, endId: Int) {
        if (endId == R.id.collapsed)
            playerViewModel.sendAction(PlayerAction.SetControlVisibility(false))
    }

    override fun onTransitionChange(p0: MotionLayout, p1: Int, p2: Int, p3: Float) {}

    override fun onTransitionCompleted(p0: MotionLayout, p1: Int) {
        handleExpanded(p1 == R.id.expanded)

        val newViewMode = when (p1) {
            R.id.fullscreen -> ViewMode.Fullscreen
            R.id.expanded -> ViewMode.Expanded
            R.id.collapsed -> ViewMode.Collapsed
            else -> ViewMode.Dismissed
        }
        playerViewModel.sendAction(PlayerAction.SetViewMode(newViewMode))
    }

    override fun onTransitionTrigger(p0: MotionLayout, p1: Int, p2: Boolean, p3: Float) {}
}