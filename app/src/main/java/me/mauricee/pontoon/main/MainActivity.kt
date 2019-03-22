package me.mauricee.pontoon.main

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.MenuItem
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.transaction
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.isupatches.wisefy.WiseFy
import com.jakewharton.rxbinding2.support.design.widget.RxBottomNavigationView
import com.jakewharton.rxbinding2.support.design.widget.RxNavigationView
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import com.ncapdevi.fragnav.FragNavController
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.layout_navigation_header.view.*
import me.mauricee.pontoon.BaseActivity
import me.mauricee.pontoon.BaseFragment
import me.mauricee.pontoon.R
import me.mauricee.pontoon.analytics.PrivacyManager
import me.mauricee.pontoon.common.gestures.GestureEvent
import me.mauricee.pontoon.common.gestures.VideoTouchHandler
import me.mauricee.pontoon.ext.*
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.login.LoginActivity
import me.mauricee.pontoon.main.creator.CreatorFragment
import me.mauricee.pontoon.main.creatorList.CreatorListFragment
import me.mauricee.pontoon.main.details.DetailsFragment
import me.mauricee.pontoon.main.history.HistoryFragment
import me.mauricee.pontoon.main.player.PlayerContract
import me.mauricee.pontoon.main.player.PlayerFragment
import me.mauricee.pontoon.main.search.SearchFragment
import me.mauricee.pontoon.main.user.UserFragment
import me.mauricee.pontoon.main.videos.VideoFragment
import me.mauricee.pontoon.model.preferences.Preferences
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video
import me.mauricee.pontoon.preferences.PreferencesActivity
import me.mauricee.pontoon.rx.glide.toPalette
import javax.inject.Inject

class MainActivity : BaseActivity(), MainContract.Navigator, MainContract.View,
        PlayerContract.Controls {

    @Inject
    lateinit var mainPresenter: MainPresenter
    @Inject
    lateinit var animationTouchListener: VideoTouchHandler
    @Inject
    lateinit var player: Player
    @Inject
    lateinit var orientationManager: OrientationManager
    @Inject
    lateinit var wiseFy: WiseFy
    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var privacyManager: PrivacyManager

    private var stayingInsideApp = false
    private val miscActions = PublishRelay.create<MainContract.Action>()
    private var currentPlayerRatio: String = "16:9"
    private val fragmentContainer: Int
        get() = R.id.main_container

    override val actions: Observable<MainContract.Action>
        get() = Observable.merge(miscActions,
                dayNightSwitch.clicks().map { MainContract.Action.NightMode },
                RxNavigationView.itemSelections(main_drawer).map { MainContract.Action.fromNavDrawer(it.itemId) })
                .doOnNext { if (it is MainContract.Action.NightMode) stayingInsideApp = true }
                .compose(checkForVideoToPlay())

    /*
    *  Setting up guideline parameters to change the
    *  guideline percent value as per user touch event
    */
    private lateinit var paramsGlHorizontal: ConstraintLayout.LayoutParams
    private lateinit var paramsGlVertical: ConstraintLayout.LayoutParams
    private lateinit var paramsGlBottom: ConstraintLayout.LayoutParams
    private lateinit var paramsGlMarginEnd: ConstraintLayout.LayoutParams
    private val constraintSet = ConstraintSet()


    private val dayNightSwitch by lazy { SwitchCompat(this) }
    private lateinit var controller: FragNavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stayingInsideApp = false
        setContentView(R.layout.activity_main)

        paramsGlHorizontal = guidelineHorizontal.layoutParams as ConstraintLayout.LayoutParams
        paramsGlVertical = guidelineVertical.layoutParams as ConstraintLayout.LayoutParams
        paramsGlBottom = guidelineBottom.layoutParams as ConstraintLayout.LayoutParams
        paramsGlMarginEnd = guidelineMarginEnd.layoutParams as ConstraintLayout.LayoutParams

        main_player.setOnTouchListener(animationTouchListener)
        controller = FragNavController.Builder(savedInstanceState, supportFragmentManager, fragmentContainer)
                .rootFragments(listOf(VideoFragment(), SearchFragment(), HistoryFragment()))
                .build()

        main_drawer.menu.findItem(R.id.action_dayNight).actionView = dayNightSwitch
        subscriptions += animationTouchListener.events.subscribe {
            when (it) {
                is GestureEvent.Click -> onClick(it.view)
                is GestureEvent.Dismiss -> onDismiss(it.view)
                is GestureEvent.Scale -> onScale(it.percentage)
                is GestureEvent.Swipe -> onSwipe(it.percentage)
                is GestureEvent.Expand -> onExpand(it.isExpanded)
            }
        }
        root.doOnLayout {
            if (player.isActive()) {
                onExpand(player.viewMode != Player.ViewMode.PictureInPicture)
                val isPortrait = isPortrait()
                if (!isPortrait) {
                    enableFullScreen(true)
                }
            } else {
                hide()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !isPortrait()) {
            enableFullScreen(true)
        }
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        toolbar?.just {
            if (controller.isRootFragment) {
                setNavigationIcon(R.drawable.ic_menu)
            } else {
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        stayingInsideApp = false
        mainPresenter.attachView(this)
        subscriptions += RxBottomNavigationView.itemSelections(main_bottomNav).subscribe(::switchTab)
        privacyManager.displayPromptIfUserHasNotBeenPrompted(this)
        player.onActive()
    }

    override fun onStop() {
        super.onStop()
        mainPresenter.detachView()
        if (!stayingInsideApp) {
            player.onPause()
            player.onInactive()
            player.viewMode = Player.ViewMode.Expanded
        }
        privacyManager.hidePromptIfOpen()
    }

    override fun onDestroy() {
        super.onDestroy()
        wiseFy.dump()
        if (isFinishing)
            player.release()
    }

    override fun setMenuExpanded(isExpanded: Boolean) {
        if (isExpanded)
            root.openDrawer(main_drawer, true)
        else
            root.closeDrawer(main_drawer, true)
    }

    override fun playVideo(video: Video, commentId: String) {
        animationTouchListener.show()
        loadFragment {
            replace(R.id.main_player, PlayerFragment.newInstance(video.thumbnail))
            replace(R.id.main_details, DetailsFragment.newInstance(video.id, commentId))
        }
        main.doOnPreDraw {
            animationTouchListener.isExpanded = true
        }
    }

    override fun toPreferences() {
        if (player.isPlaying()) player.onPause()
        PreferencesActivity.navigateTo(this)
    }

    override fun toCreator(creator: UserRepository.Creator) {
        controller.pushFragment(CreatorFragment.newInstance(creator.id, creator.name))
        if (player.isActive()) {
            animationTouchListener.isExpanded = false
            setPlayerExpanded(false)
        }
    }

    override fun toCreatorsList() {
        controller.pushFragment(CreatorListFragment.newInstance())
        if (player.isActive()) {
            animationTouchListener.isExpanded = false
            setPlayerExpanded(false)
        }
    }

    override fun toUser(user: UserRepository.User) {
        controller.pushFragment(UserFragment.newInstance(user.id))
        if (player.isActive()) {
            animationTouchListener.isExpanded = false
            setPlayerExpanded(false)
        }
        root.closeDrawer(main_drawer)
    }

    override fun setVideoRatio(ratio: String) {
        currentPlayerRatio = ratio
        main.updateParams {
            setDimensionRatio(main_player.id, "h,$ratio")
            TransitionManager.beginDelayedTransition(main, ChangeBounds().apply {
                duration = 150
            })
        }
    }

    override fun toggleFullscreen() {
        orientationManager.isFullscreen = !orientationManager.isFullscreen
    }

    private fun onClick(view: View) {
        miscActions.accept(MainContract.Action.PlayerClicked)
    }

    private fun onDismiss(view: View) {
        dismiss()
    }

    private fun onScale(percentage: Float) {
        if (isPortrait())
            scaleVideo(percentage)
        else {
            player_display?.scaleVideo(percentage)
        }
    }

    private fun onSwipe(percentage: Float) {
        swipeVideo(percentage)
    }

    private fun onExpand(isExpanded: Boolean) {
        logd("expand: $isExpanded")
        setPlayerExpanded(isExpanded)
    }

    override fun updateState(state: MainContract.State) = when (state) {
        is MainContract.State.CurrentUser -> displayUser(state.user, state.subCount)
        is MainContract.State.Logout -> {
            if (player.isActive()) player.onStop()
            LoginActivity.navigateTo(this)
            finishAffinity()
        }

        MainContract.State.SessionExpired -> {
            AlertDialog.Builder(this)
                    .setTitle(R.string.main_session_expired_title)
                    .setMessage(R.string.main_session_expired_body)
                    .setPositiveButton(android.R.string.ok) { _, _ -> miscActions.accept(MainContract.Action.Expired) }
                    .setCancelable(false)
                    .create().show()

        }
        is MainContract.State.NightMode -> dayNightSwitch.isChecked = state.isInNightMode
    }

    override fun onBackPressed() {
        if (orientationManager.isFullscreen) {
            orientationManager.isFullscreen = false
        } else if (root.isDrawerOpen(main_drawer)) {
            root.closeDrawer(main_drawer, true)
        } else if (animationTouchListener.isExpanded && player.isActive()) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pip = preferences.pictureInPicture
            when {
                pip == Preferences.PictureInPicture.Always && player.isActive() -> goIntoPip()
                pip == Preferences.PictureInPicture.OnlyWhenPlaying && player.isPlaying() -> goIntoPip()
            }

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isNotInPip = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode
        if (isNotInPip) {
            enableFullScreen(!isPortrait())
            supportFragmentManager.findFragmentById(R.id.main_player)?.with {
                supportFragmentManager.transaction {
                    detach(it)
                    attach(it)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun goIntoPip() {
        orientationManager.isFullscreen = false
        expandPlayerTo(true, Player.ViewMode.FullScreen)
        enterPictureInPictureMode(PictureInPictureParams.Builder()
                .setAspectRatio(Rational.parseRational(currentPlayerRatio))
                .build())
        player.viewMode = Player.ViewMode.PictureInPicture
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        enableFullScreen(isInPictureInPictureMode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.with(controller::onSaveInstanceState)
    }


    private fun switchTab(item: MenuItem) {
        val newTab = when (item.itemId) {
            R.id.nav_home -> 0
            R.id.nav_search -> 1
            R.id.nav_history -> 2
            else -> throw RuntimeException("Invalid tab selection")
        }
        if (newTab == controller.currentStackIndex)
            if (controller.isRootFragment) (controller.currentFrag as? BaseFragment<*>)?.reset()
            else controller.clearStack()
        else
            controller.switchTab(newTab)
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

        paramsGlVertical.guidePercent = percentHorizontalMoved
        paramsGlMarginEnd.guidePercent = percentMarginMoved

        guidelineVertical.layoutParams = paramsGlVertical
        guidelineMarginEnd.layoutParams = paramsGlMarginEnd
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
            doAfter { player.onStop() }
        })
    }

    /**
     * Expand or collapse the video fragment animation
     */
    override fun setPlayerExpanded(isExpanded: Boolean) = expandPlayerTo(isExpanded, Player.ViewMode.Expanded)

    private fun expandPlayerTo(isExpanded: Boolean, expandedState: Player.ViewMode) {
        main_player.alpha = 1f
        main.updateParams(constraintSet) {
            setGuidelinePercent(guidelineHorizontal.id, if (isExpanded) 0F else VideoTouchHandler.MIN_VERTICAL_LIMIT)
            setGuidelinePercent(guidelineVertical.id, if (isExpanded) 0F else VideoTouchHandler.MIN_HORIZONTAL_LIMIT)
            setGuidelinePercent(guidelineBottom.id, if (isExpanded) 1F else VideoTouchHandler.MIN_BOTTOM_LIMIT)
            setGuidelinePercent(guidelineMarginEnd.id, if (isExpanded) 1F else VideoTouchHandler.MIN_MARGIN_END_LIMIT)
            setAlpha(main_details.id, if (isExpanded) 1.0F else 0F)

            TransitionManager.beginDelayedTransition(main, ChangeBounds().apply {
                interpolator = android.view.animation.AnticipateOvershootInterpolator(1.0f)
                duration = 250
                doAfter { player.viewMode = if (isExpanded) expandedState else Player.ViewMode.PictureInPicture }
            })
        }
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
                        doAfter { removeFragmentByID(R.id.main_player) }
                    })
        }
    }

    private fun enableFullScreen(isEnabled: Boolean) {
        if (isEnabled) {
            root.doOnPreDraw {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            }
            root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            main.updateParams(constraintSet) {
                connect(main_player.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(main_player.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                connect(main_player.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(main_player.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                setDimensionRatio(main_player.id, "")
            }
            player.viewMode = Player.ViewMode.FullScreen
        } else {
            root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            main.updateParams(constraintSet) {
                connect(main_player.id, ConstraintSet.START, guidelineVertical.id, ConstraintSet.START)
                connect(main_player.id, ConstraintSet.END, guidelineMarginEnd.id, ConstraintSet.END)
                connect(main_player.id, ConstraintSet.TOP, guidelineHorizontal.id, ConstraintSet.BOTTOM)
                clear(main_player.id, ConstraintSet.BOTTOM)
                setDimensionRatio(main_player.id, currentPlayerRatio)
            }
            player.viewMode = Player.ViewMode.Expanded
        }
        animationTouchListener.pinchToZoomEnabled = isEnabled
    }

    private fun displayUser(user: UserRepository.User, subCount: Int) {
        main_drawer.getHeaderView(0).apply {
            user.with { user ->
                findViewById<TextView>(R.id.header_title).text = user.username
                findViewById<TextView>(R.id.header_subtitle).text = getString(R.string.home_subtitle_suffix, subCount)
                subscriptions += GlideApp.with(this).asBitmap().load(user.profileImage)
                        .toPalette().subscribe { palette ->
                            findViewById<ImageView>(R.id.header_icon).setImageBitmap(palette.bitmap)
                            themeManager.getVibrantSwatch(palette.palette).apply {
                                findViewById<ImageView>(R.id.header).setBackgroundColor(rgb)
                                header_title.setTextColor(titleTextColor)
                                header_subtitle.setTextColor(bodyTextColor)
                            }
                        }
            }
        }
    }

    private fun checkForVideoToPlay(): (Observable<MainContract.Action>) -> Observable<MainContract.Action> {
        return {
            if (intent.hasExtra(VideoToPlayKey)) {
                val id = intent.getStringExtra(VideoToPlayKey)
                intent.removeExtra(VideoToPlayKey)
                it.startWith(MainContract.Action.PlayVideo(id))
            } else
                it
        }
    }

    companion object {
        private const val VideoToPlayKey = "VideoToPlay"

        fun navigateTo(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        fun playVideo(context: Context, videoId: String) = context.startActivity(Intent(context, MainActivity::class.java)
                .putExtra(VideoToPlayKey, videoId).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }
}
