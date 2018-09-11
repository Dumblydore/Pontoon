package me.mauricee.pontoon.main

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.MenuItem
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.transition.*
import com.jakewharton.rxbinding2.support.design.widget.RxBottomNavigationView
import com.jakewharton.rxbinding2.support.design.widget.RxNavigationView
import com.jakewharton.rxrelay2.PublishRelay
import com.ncapdevi.fragnav.FragNavController
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import me.mauricee.pontoon.BaseActivity
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.gestures.GestureEvents
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.preferences.PreferencesActivity
import javax.inject.Inject

class MainActivity : BaseActivity(), MainContract.Navigator, GestureEvents, MainContract.View {

    @Inject
    lateinit var mainPresenter: MainPresenter
    @Inject
    lateinit var animationTouchListener: VideoTouchHandler
    @Inject
    lateinit var player: Player
    @Inject
    lateinit var orientationManager: OrientationManager

    private val miscActions = PublishRelay.create<MainContract.Action>()

    private val fragmentContainer: Int
        get() = R.id.main_container

    override val actions: Observable<MainContract.Action>
        get() = Observable.merge(miscActions, RxNavigationView.itemSelections(main_drawer).map { MainContract.Action.fromNavDrawer(it.itemId) })
    /*
    *  Setting up guideline parameters to change the
    *  guideline percent value as per user touch event
    */
    private lateinit var paramsGlHorizontal: ConstraintLayout.LayoutParams
    private lateinit var paramsGlVertical: ConstraintLayout.LayoutParams
    private lateinit var paramsGlBottom: ConstraintLayout.LayoutParams
    private lateinit var paramsGlMarginEnd: ConstraintLayout.LayoutParams
    private val constraintSet = ConstraintSet()


    private lateinit var controller: FragNavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paramsGlHorizontal = guidelineHorizontal.layoutParams as ConstraintLayout.LayoutParams
        paramsGlVertical = guidelineVertical.layoutParams as ConstraintLayout.LayoutParams
        paramsGlBottom = guidelineBottom.layoutParams as ConstraintLayout.LayoutParams
        paramsGlMarginEnd = guidelineMarginEnd.layoutParams as ConstraintLayout.LayoutParams

        main_player.setOnTouchListener(animationTouchListener)
        hide()

        controller = FragNavController.Builder(savedInstanceState, supportFragmentManager, fragmentContainer)
                .rootFragments(listOf(VideoFragment(), SearchFragment(), HistoryFragment()))
                .build()

        subscriptions += RxBottomNavigationView.itemSelections(main_bottomNav).subscribe(::switchTab)
    }

    override fun onStart() {
        super.onStart()
        mainPresenter.attachView(this)
    }

    override fun onStop() {
        super.onStop()
        player.onPause()
        mainPresenter.detachView()
    }

    override fun playVideo(video: Video, commentId: String) {
        animationTouchListener.show()
        main_player.alpha = 1f
        loadFragment {
            replace(R.id.main_player, PlayerFragment.newInstance(video.thumbnail))
        }
        loadFragment {
            replace(R.id.main_details, DetailsFragment.newInstance(video.id, commentId))
        }
        main.doOnPreDraw {
            animationTouchListener.isExpanded = true
            setPlayerExpanded(true)
        }
    }

    override fun toCreator(creator: UserRepository.Creator) {
        controller.pushFragment(CreatorFragment.newInstance(creator.id))
        animationTouchListener.isExpanded = false
        setPlayerExpanded(false)
    }

    override fun toCreatorsList() {
        controller.pushFragment(CreatorListFragment.newInstance())
        animationTouchListener.isExpanded = false
        setPlayerExpanded(false)
    }

    override fun toUser(user: UserRepository.User) {
        controller.pushFragment(UserFragment.newInstance(user.id))
        animationTouchListener.isExpanded = false
        setPlayerExpanded(false)
    }

    override fun onClick(view: View) {
        if (!animationTouchListener.isExpanded) {
            animationTouchListener.isExpanded = true
        } else {
            miscActions.accept(MainContract.Action.ClickEvent)
        }
    }

    override fun onDismiss(view: View) {
        dismiss()
    }

    override fun onScale(percentage: Float) {
        scaleVideo(percentage)
    }

    override fun onSwipe(percentage: Float) {
        swipeVideo(percentage)
    }

    override fun onExpand(isExpanded: Boolean) {
        setPlayerExpanded(isExpanded)
    }

    override fun updateState(state: MainContract.State) = when (state) {
        is MainContract.State.CurrentUser -> displayUser(state.user, state.subCount)
        is MainContract.State.Preferences -> PreferencesActivity.navigateTo(this)
        is MainContract.State.Logout -> LoginActivity.navigateTo(this)
    }.also { root.closeDrawer(main_drawer, true) }

    override fun onBackPressed() {
        if (orientationManager.isFullscreen) {
            orientationManager.isFullscreen = false
        } else if (root.isDrawerOpen(main_drawer)) {
            root.closeDrawer(main_drawer, true)
        } else if (animationTouchListener.isExpanded) {
            animationTouchListener.isExpanded = false
            if (!isPortrait()) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } else if (!controller.isRootFragment) {
            controller.popFragment()
        } else {
            super.onBackPressed()
        }
    }

    override fun onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && player.isPlaying()) {
            setPlayerExpanded(true)
            enterPictureInPictureMode(PictureInPictureParams.Builder()
                    .setAspectRatio(Rational.parseRational("16:9"))
                    .build())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val isPortrait = isPortrait() || newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT
        if (isPortrait) {

            animationTouchListener.isEnabled = true
            enableFullScreen(false)
        } else {
            animationTouchListener.isEnabled = false
            if (!animationTouchListener.isExpanded) {
                animationTouchListener.isExpanded = true
            }
            enableFullScreen(true)
        }

        //Update this params in last after all configuration changes are done
        main.updateParams(constraintSet) {
            constrainHeight(main_player.id, if (isPortrait) 0 else getDeviceHeight())
            constrainWidth(main_player.id, if (isPortrait) 0 else getDeviceWidth())
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        enableFullScreen(isInPictureInPictureMode)
    }

    private fun switchTab(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_home -> 0
            R.id.nav_search -> 1
            R.id.nav_history -> 2
            else -> throw RuntimeException("Invalid tab selection")
        }.let(controller::switchTab)
    }

    /**
     * Scale the video as per given percentage of user scrolls
     * in up/down direction from current position
     */
    private fun scaleVideo(percentScrollUp: Float) {

        //Prevent guidelines to go out of screen bound
        val percentVerticalMoved = Math.max(0F, Math.min(VideoTouchHandler.MIN_VERTICAL_LIMIT, percentScrollUp))
        val movedPercent = percentVerticalMoved / VideoTouchHandler.MIN_VERTICAL_LIMIT
        val percentHorizontalMoved = VideoTouchHandler.MIN_HORIZONTAL_LIMIT * movedPercent
        val percentBottomMoved = 1F - movedPercent * (1F - VideoTouchHandler.MIN_BOTTOM_LIMIT)
        loge("Bottom : $percentBottomMoved")
        val percentMarginMoved = 1F - movedPercent * (1F - VideoTouchHandler.MIN_MARGIN_END_LIMIT)

        paramsGlHorizontal.guidePercent = percentVerticalMoved
        paramsGlVertical.guidePercent = percentHorizontalMoved
        paramsGlBottom.guidePercent = percentBottomMoved
        paramsGlMarginEnd.guidePercent = percentMarginMoved

        guidelineHorizontal.layoutParams = paramsGlHorizontal
        guidelineVertical.layoutParams = paramsGlVertical
        guidelineBottom.layoutParams = paramsGlBottom
        guidelineMarginEnd.layoutParams = paramsGlMarginEnd

        main_details.alpha = 1.0F - movedPercent
    }

    /**
     * Swipe animation on given percentage user has scroll on left/right
     * direction from the current position
     */
    private fun swipeVideo(percentScrollSwipe: Float) {
        //Prevent guidelines to go out of screen bound
        val percentHorizontalMoved = Math.max(-0.25F, Math.min(VideoTouchHandler.MIN_HORIZONTAL_LIMIT, percentScrollSwipe))
        val percentMarginMoved = percentHorizontalMoved + (VideoTouchHandler.MIN_MARGIN_END_LIMIT - VideoTouchHandler.MIN_HORIZONTAL_LIMIT)
        val movedPercent = percentHorizontalMoved  / VideoTouchHandler.MIN_HORIZONTAL_LIMIT

        paramsGlVertical.guidePercent = percentHorizontalMoved
        paramsGlMarginEnd.guidePercent = percentMarginMoved

        guidelineVertical.layoutParams = paramsGlVertical
        guidelineMarginEnd.layoutParams = paramsGlMarginEnd
        main_player.alpha = movedPercent

    }

    /**
     * Hide all video and video details fragment
     */
    private fun hide() = main.updateParams {
        setGuidelinePercent(guidelineHorizontal.id, 100F)
        setGuidelinePercent(guidelineVertical.id, 100F)
        setAlpha(main_details.id, 0F)

        TransitionManager.beginDelayedTransition(main, ChangeBounds().apply {
            interpolator = AnticipateOvershootInterpolator(1.0f)
            duration = 250
        })
    }

    /**
     * Expand or collapse the video fragment animation
     */
    override fun setPlayerExpanded(isExpanded: Boolean) = main.updateParams(constraintSet) {
        setGuidelinePercent(guidelineHorizontal.id, if (isExpanded) 0F else VideoTouchHandler.MIN_VERTICAL_LIMIT)
        setGuidelinePercent(guidelineVertical.id, if (isExpanded) 0F else VideoTouchHandler.MIN_HORIZONTAL_LIMIT)
        setGuidelinePercent(guidelineBottom.id, if (isExpanded) 1F else VideoTouchHandler.MIN_BOTTOM_LIMIT)
        setGuidelinePercent(guidelineMarginEnd.id, if (isExpanded) 1F else VideoTouchHandler.MIN_MARGIN_END_LIMIT)
        setAlpha(main_details.id, if (isExpanded) 1.0F else 0F)

        TransitionManager.beginDelayedTransition(main, ChangeBounds().apply {
            interpolator = android.view.animation.AnticipateOvershootInterpolator(1.0f)
            duration = 250
        })
    }

    /**
     * Show dismiss animation when user have moved
     * more than 50% horizontally
     */
    private fun dismiss() {
        player.onStop()
        main.updateParams(constraintSet) {
            setGuidelinePercent(guidelineVertical.id, VideoTouchHandler.MIN_HORIZONTAL_LIMIT - VideoTouchHandler.MIN_MARGIN_END_LIMIT)
            setGuidelinePercent(guidelineMarginEnd.id, 0F)
            TransitionManager.beginDelayedTransition(main, TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(Fade()).apply {
                        interpolator = AnticipateOvershootInterpolator(1.0f)
                        duration = 250
                        addListener(object : Transition.TransitionListener {
                            override fun onTransitionResume(transition: Transition) {
                            }

                            override fun onTransitionPause(transition: Transition) {
                            }

                            override fun onTransitionCancel(transition: Transition) {
                            }

                            override fun onTransitionStart(transition: Transition) {
                            }

                            override fun onTransitionEnd(transition: Transition) {
//                    //Remove Video when swipe animation is ended
                                removeFragmentByID(R.id.main_player)
                            }
                        })
                    })
        }
    }

    private fun enableFullScreen(isEnabled: Boolean) {
        if (isEnabled) {
            root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun displayUser(user: UserRepository.User, subCount: Int) {
        main_drawer.getHeaderView(0).apply {
            user.let {
                findViewById<TextView>(R.id.header_title).text = it.username
                findViewById<TextView>(R.id.header_subtitle).text = getString(R.string.home_subtitle_suffix, subCount)
                GlideApp.with(this).load(it.profileImage)
                        .placeholder(R.drawable.ic_default_thumbnail)
                        .error(R.drawable.ic_default_thumbnail)
                        .into(findViewById(R.id.header_icon))
            }
        }
    }
}
