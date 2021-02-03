package me.mauricee.pontoon.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.jakewharton.rxbinding2.view.clicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxkotlin.plusAssign
import me.mauricee.pontoon.R
import me.mauricee.pontoon.databinding.FragmentMainBinding
import me.mauricee.pontoon.ext.map
import me.mauricee.pontoon.ext.mapDistinct
import me.mauricee.pontoon.ext.notNull
import me.mauricee.pontoon.ext.view.viewBinding
import me.mauricee.pontoon.playback.NewPlayer
import me.mauricee.pontoon.ui.NewBaseFragment
import me.mauricee.pontoon.ui.main.player.PlayerState
import me.mauricee.pontoon.ui.main.player.PlayerViewModel
import me.mauricee.pontoon.ui.main.player.ViewMode
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : NewBaseFragment(R.layout.fragment_main), MainContract.Navigator, MotionLayout.TransitionListener {

    @Inject
    lateinit var player: NewPlayer

    @Inject
    lateinit var orientationManager: OrientationManager

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

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MainContract.Event.TriggerNightMode -> TODO()
                is MainContract.Event.NavigateToUser -> TODO()
                MainContract.Event.ToggleMenu -> setMenuExpanded(true)
                MainContract.Event.NavigateToPreferences -> TODO()
                MainContract.Event.NavigateToLoginScreen -> TODO()
                MainContract.Event.SessionExpired -> TODO()
            }
        }

        playerViewModel.state.map(PlayerState::viewMode).observe(viewLifecycleOwner) {
            when (it) {
                ViewMode.Dismissed -> {
                }
                ViewMode.Expanded -> {
                    orientationManager.isFullscreen = false
                    playVideo()
                }
                ViewMode.Fullscreen -> {
                    orientationManager.isFullscreen = true
                    binding.main.transitionToState(R.id.fullscreen)
                }
            }
        }
        playerViewModel.state.mapDistinct { it.video?.entity?.title }.notNull().observe(viewLifecycleOwner) {
            binding.collapsedDetailsTitle.text = it
        }
        playerViewModel.state.mapDistinct { it.video?.creator?.entity?.name }.notNull().observe(viewLifecycleOwner) {
            binding.collapsedDetailsSubtitle.text = it
        }
    }

    override fun playVideo(videoId: String, commentId: String) {
        binding.main.transitionToEnd()
    }

    override fun setMenuExpanded(isExpanded: Boolean) {
        if (binding.root.isDrawerOpen(binding.mainDrawer)) {
            binding.root.closeDrawer(binding.mainDrawer)
        } else {
            binding.root.openDrawer(binding.mainDrawer)
        }
    }

    private fun handleExpanded(isExpanded: Boolean) {
        if (isExpanded) {
            binding.main.setTransition(R.id.transition_active)
            if (binding.mainContainerPreview.isGone) {
                val w = binding.root.measuredWidth
                val h = binding.root.measuredHeight

                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                binding.mainContainer.draw(canvas)

                binding.mainContainerPreview.setImageBitmap(bitmap)
                binding.mainContainerPreview.isGone = false
            }
            binding.mainContainer.isGone = isExpanded
        } else {
            binding.mainContainer.isGone = isExpanded
            binding.mainContainer.doOnNextLayout {
                binding.mainContainerPreview.isGone = true
            }
        }
    }

    private fun playVideo() {
        when (binding.main.currentState) {
            R.id.fullscreen,
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

    override fun onTransitionStarted(p0: MotionLayout, p1: Int, p2: Int) {}

    override fun onTransitionChange(p0: MotionLayout, p1: Int, p2: Int, p3: Float) {}

    override fun onTransitionCompleted(p0: MotionLayout, p1: Int) {
        when (p0.currentState) {
            R.id.fullscreen -> binding.mainContainer.isGone = true
            else -> handleExpanded(p0.currentState == R.id.expanded)
        }
    }

    override fun onTransitionTrigger(p0: MotionLayout, p1: Int, p2: Boolean, p3: Float) {}
}