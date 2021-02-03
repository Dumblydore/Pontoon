package me.mauricee.pontoon.ui.main


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity  {

//    @Inject
//    lateinit var animationTouchListener: VideoTouchHandler
//
//    @Inject
//    lateinit var newPlayer: NewPlayer
//
//    @Inject
//    lateinit var orientationManager: OrientationManager
//
//    @Inject
//    lateinit var wiseFy: WiseFy
//
//    @Inject
//    lateinit var preferences: Preferences
//
//    @Inject
//    lateinit var privacyManager: PrivacyManager
//
//    @Inject
//    lateinit var playerFactory: PlayerFactory
//
//
////    @Inject
////    lateinit var factory: MainContract.ViewModel.Factory
//
//    val playerViewModel: PlayerViewModel by viewModels()
//    private val mainViewModel: MainContract.ViewModel by viewModels()// { factory }
//
//    private var stayingInsideApp = false
//    private var currentPlayerRatio: String = "16:9"
//    private val fragmentContainer: Int
//        get() = R.id.main_container
//
//    /*
//    *  Setting up binding.guideline parameters to change the
//    *  binding.guideline percent value as per user touch event
//    */
//    private lateinit var paramsGlHorizontal: ConstraintLayout.LayoutParams
//    private lateinit var paramsGlVertical: ConstraintLayout.LayoutParams
//    private lateinit var paramsGlBottom: ConstraintLayout.LayoutParams
//    private lateinit var paramsGlMarginEnd: ConstraintLayout.LayoutParams
//    private val constraintSet = ConstraintSet()
//
//
//    private val dayNightSwitch by lazy { SwitchCompat(this) }
//    private lateinit var controller: FragNavController
//    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        stayingInsideApp = false
//        setContentView(binding.root)
//
//        bitmap = savedInstanceState?.getParcelable("TEST")
//        bitmap?.let {
//            logd("recreated delegate")
//            binding.reveal.setImageBitmap(it)
//            binding.reveal.isVisible = true
//            binding.reveal.doOnPreDraw {
//                val w = binding.root.measuredWidth
//                val h = binding.root.measuredHeight
//                val finalRadius = hypot(w.toFloat(), h.toFloat())
//                val anim = ViewAnimationUtils.createCircularReveal(binding.root, w / 2, h / 2, 0f, finalRadius)
//                anim.duration = 400L
//                anim.doOnEnd {
//                    binding.reveal.setImageDrawable(null)
//                    binding.reveal.isVisible = false
//                }
//                anim.start()
//            }
//        }
//
//        paramsGlHorizontal = binding.guidelineHorizontal.layoutParams as ConstraintLayout.LayoutParams
//        paramsGlVertical = binding.guidelineVertical.layoutParams as ConstraintLayout.LayoutParams
//        paramsGlBottom = binding.guidelineBottom.layoutParams as ConstraintLayout.LayoutParams
//        paramsGlMarginEnd = binding.guidelineMarginEnd.layoutParams as ConstraintLayout.LayoutParams
//
//        binding.mainPlayer.setOnTouchListener(animationTouchListener)
//        controller = FragNavController.Builder(savedInstanceState, supportFragmentManager, fragmentContainer)
//                .rootFragments(listOf(VideoFragment(), SearchFragment(), HistoryFragment()))
//                .build()
//
//        binding.mainDrawer.menu.findItem(R.id.action_dayNight).actionView = dayNightSwitch
////        playerViewModel.watchStateValue { viewMode }.observe(this) { viewMode ->
////            when (viewMode) {
////                is ViewMode.None -> if (viewMode.dismissed) dismiss() else hide()
////                is ViewMode.Scale -> scaleVideo(viewMode.percent)
////                is ViewMode.Swipe -> swipeVideo(viewMode.percent)
////                ViewMode.PictureInPicture -> expandPlayerTo(false, viewMode)
////                is ViewMode.FullScreen -> enableFullScreen(true)
////                is ViewMode.Expanded -> expandPlayerTo(true, viewMode)
////            }
////        }
//        playerFactory.bind(this)
//
//
////        Observable.merge(
////                miscActions,
////                RxNavigationView.itemSelections(binding.root).map { MainContract.Action.fromNavDrawer(it.itemId) }
////        ).doOnNext { if (it is MainContract.Action.NightMode) stayingInsideApp = true }
////                .compose(checkForVideoToPlay())
//
//        subscriptions += binding.mainDrawer.menu.findItem(R.id.action_dayNight).actionView.clicks().map { MainContract.Action.NightMode }
//                .subscribe(mainViewModel::sendAction)
////        subscriptions += binding.root.itemSele
//
//        mainViewModel.state.mapDistinct(MainContract.State::user).notNull().observe(this, ::displayUser)
//        mainViewModel.state.mapDistinct(MainContract.State::subCount).notNull().observe(this, ::displaySubCount)
//        mainViewModel.state.mapDistinct(MainContract.State::isNightModeEnabled).observe(this, dayNightSwitch::setChecked)
//
//        mainViewModel.events.observe(this, ::handleEvents)
//    }
//
//
//    private fun handleEvents(event: MainContract.Event) {
//        when (event) {
//            is MainContract.Event.TriggerNightMode -> changeNightMode(event)
//            is MainContract.Event.NavigateToUser -> TODO()//toUser(event.user.id)
//            MainContract.Event.NavigateToPreferences -> PreferencesActivity.navigateTo(this)
////            MainContract.Event.NavigateToLoginScreen -> LoginActivity.navigateTo(this)
//            MainContract.Event.SessionExpired -> TODO()
//        }
//    }
//
//    private fun changeNightMode(event: MainContract.Event.TriggerNightMode) {
//        if (binding.reveal.isVisible)
//            return
//        val w = binding.root.measuredWidth
//        val h = binding.root.measuredHeight
//
//        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap!!)
//        binding.root.draw(canvas)
//
//        binding.reveal.setImageBitmap(bitmap)
//        binding.reveal.isVisible = true
//
//        AppCompatDelegate.setDefaultNightMode(event.mode)
//
//        logd("After delegate")
//    }
//
//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus && !isPortrait()) {
//            enableFullScreen(true)
//        }
//    }
//
//    override fun setSupportActionBar(toolbar: Toolbar?) {
//        super.setSupportActionBar(toolbar)
//        toolbar?.just {
//            if (controller.isRootFragment) {
//                setNavigationIcon(R.drawable.ic_menu)
//            } else {
//                setNavigationIcon(R.drawable.ic_back)
//                setNavigationOnClickListener { onBackPressed() }
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        stayingInsideApp = false
////        mainPresenter.attachView(this)
////        subscriptions += RxBottomNavigationView.itemSelections(main_bottomNav).subscribe(::switchTab)
//        privacyManager.displayPromptIfUserHasNotBeenPrompted(this)
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (!stayingInsideApp) {
////            playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.Expanded(false)))
//        }
//        privacyManager.hidePromptIfOpen()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        wiseFy.dump()
//    }
//
//    override fun setMenuExpanded(isExpanded: Boolean) {
////        mainViewModel.sendAction(MainContract.Action.NightMode)
//        if (isExpanded)
//            binding.root.openDrawer(binding.mainDrawer, true)
//        else
//            binding.root.closeDrawer(binding.mainDrawer, true)
//    }
//
//    override fun playVideo(videoId: String, commentId: String) {
//        playerViewModel.sendAction(PlayerAction.PlayVideo(videoId, commentId))
//        animationTouchListener.show()
//        loadFragment {
//            replace(binding.mainPlayer.id, PlayerFragment.newInstance(videoId))
//            replace(R.id.main_details, DetailsFragment.newInstance(videoId, commentId))
//        }
//        binding.main.doOnPreDraw {
//            animationTouchListener.isExpanded = true
//        }
//    }
//
////    override fun toPreferences() {
////        subscriptions += newPlayer.pause().subscribe {
////            PreferencesActivity.navigateTo(this)
////        }
////    }
//
////    override fun toCreator(creatorId: String) {
////        controller.pushFragment(CreatorFragment.newInstance(creatorId))
////        playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.PictureInPicture))
////    }
////
////    override fun toCreatorsList() {
////        controller.pushFragment(CreatorListFragment.newInstance())
////        playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.PictureInPicture))
////    }
////
////    override fun toUser(userId: String) {
////        controller.pushFragment(UserFragment.newInstance(userId))
////        playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.PictureInPicture))
////        binding.root.closeDrawer(binding.root)
////    }
//
//    override fun setVideoRatio(ratio: String) {
//        if (!orientationManager.isFullscreen) {
//            currentPlayerRatio = ratio
//            binding.main.updateParams {
//                setDimensionRatio(binding.mainPlayer.id, "h,$ratio")
//                TransitionManager.beginDelayedTransition(binding.main, ChangeBounds().apply {
//                    duration = 150
//                })
//            }
//        }
//    }
//
//    override fun toggleFullscreen() {
//        orientationManager.isFullscreen = !orientationManager.isFullscreen
//    }
//
////    override fun updateState(state: MainContract.State) = when (state) {
////        is MainContract.State.CurrentUser -> displayUser(state.user, state.subCount)
////        is MainContract.State.Logout -> {
////            LoginActivity.navigateTo(this)
////            finishAffinity()
////        }
////
////        MainContract.State.SessionExpired -> {
////            AlertDialog.Builder(this)
////                    .setTitle(R.string.main_session_expired_title)
////                    .setMessage(R.string.main_session_expired_body)
////                    .setPositiveButton(android.R.string.ok) { _, _ -> miscActions.accept(MainContract.Action.Expired) }
////                    .setCancelable(false)
////                    .create().show()
////
////        }
////        is MainContract.State.NightMode ->
////    }
//
//    override fun onBackPressed() {
//        if (orientationManager.isFullscreen) {
//            orientationManager.isFullscreen = false
//        } else if (binding.root.isDrawerOpen(binding.root)) {
//            binding.root.closeDrawer(binding.root, true)
//        } else if (animationTouchListener.isExpanded) {
//            animationTouchListener.isExpanded = false
//            if (!isPortrait()) {
//                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            }
//        } else if (!controller.isRootFragment) {
//            controller.popFragment()
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onUserLeaveHint() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && newPlayer.isPlayingLocally) {
//            val pip = preferences.pictureInPicture
////            when {
//            /*pip == Preferences.PictureInPicture.Always ->*/ goIntoPip()
////                pip == Preferences.PictureInPicture.OnlyWhenPlaying && player.isPlaying -> goIntoPip()
////            }
//
//        }
//    }
//
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        val isNotInPip = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode
//        if (isNotInPip) {
//            enableFullScreen(!isPortrait())
//            supportFragmentManager.findFragmentById(binding.mainPlayer.id)?.with {
//                supportFragmentManager.transaction {
//                    detach(it)
//                    attach(it)
//                }
//            }
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun goIntoPip() {
//        orientationManager.isFullscreen = false
////        playerViewModel.sendAction(PlayerAction.SetViewMode(ViewMode.FullScreen(true)))
//        enterPictureInPictureMode(PictureInPictureParams.Builder()
//                .setAspectRatio(Rational.parseRational(currentPlayerRatio))
//                .build())
//    }
//
//    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
//        enableFullScreen(isInPictureInPictureMode)
//    }
//
//    private var bitmap: Bitmap? = null
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        bitmap?.let { outState.putParcelable("TEST", bitmap) }
//        outState.with(controller::onSaveInstanceState)
//    }
//
//
//    private fun switchTab(item: MenuItem) {
////        val newTab = when (item.itemId) {
////            R.id.nav_home -> 0
////            R.id.nav_search -> 1
////            R.id.nav_history -> 2
////            else -> throw RuntimeException("Invalid tab selection")
////        }
////        if (newTab == controller.currentStackIndex)
////            if (controller.isRootFragment) (controller.currentFrag as? BaseFragment<*>)?.reset()
////            else controller.clearStack()
////        else
////            controller.switchTab(newTab)
//    }
//
//    /**
//     * Scale the video as per given percentage of user scrolls
//     * in up/down direction from current position
//     */
//    private fun scaleVideo(percentScrollUp: Float) {
//
//        //Prevent binding.guidelines to go out of screen bound
//        val percentVerticalMoved = Math.max(0F, Math.min(VideoTouchHandler.MIN_VERTICAL_LIMIT, percentScrollUp))
//        val movedPercent = percentVerticalMoved / VideoTouchHandler.MIN_VERTICAL_LIMIT
//        val percentHorizontalMoved = VideoTouchHandler.MIN_HORIZONTAL_LIMIT * movedPercent
//        val percentBottomMoved = 1F - movedPercent * (1F - VideoTouchHandler.MIN_BOTTOM_LIMIT)
//        val percentMarginMoved = 1F - movedPercent * (1F - VideoTouchHandler.MIN_MARGIN_END_LIMIT)
//
//        paramsGlHorizontal.guidePercent = percentVerticalMoved
//        paramsGlVertical.guidePercent = percentHorizontalMoved
//        paramsGlBottom.guidePercent = percentBottomMoved
//        paramsGlMarginEnd.guidePercent = percentMarginMoved
//
//        binding.guidelineHorizontal.layoutParams = paramsGlHorizontal
//        binding.guidelineVertical.layoutParams = paramsGlVertical
//        binding.guidelineBottom.layoutParams = paramsGlBottom
//        binding.guidelineMarginEnd.layoutParams = paramsGlMarginEnd
//
//        binding.mainDetails.alpha = 1.0F - movedPercent
//    }
//
//    /**
//     * Swipe animation on given percentage user has scroll on left/right
//     * direction from the current position
//     */
//    private fun swipeVideo(percentScrollSwipe: Float) {
//        //Prevent binding.guidelines to go out of screen bound
//        val percentHorizontalMoved = Math.max(-0.25F, Math.min(VideoTouchHandler.MIN_HORIZONTAL_LIMIT, percentScrollSwipe))
//        val percentMarginMoved = percentHorizontalMoved + (VideoTouchHandler.MIN_MARGIN_END_LIMIT - VideoTouchHandler.MIN_HORIZONTAL_LIMIT)
//
//        paramsGlVertical.guidePercent = percentHorizontalMoved
//        paramsGlMarginEnd.guidePercent = percentMarginMoved
//
//        binding.guidelineVertical.layoutParams = paramsGlVertical
//        binding.guidelineMarginEnd.layoutParams = paramsGlMarginEnd
//    }
//
//    /**
//     * Hide all video and video details fragment
//     */
//    private fun hide() = binding.main.updateParams {
//        setGuidelinePercent(binding.guidelineHorizontal.id, 100F)
//        setGuidelinePercent(binding.guidelineVertical.id, 100F)
//        setAlpha(binding.mainDetails.id, 0F)
//
//        TransitionManager.beginDelayedTransition(binding.main, ChangeBounds().apply {
//            interpolator = AnticipateOvershootInterpolator(1.0f)
//            duration = 250
//        })
//    }
//
//    /**
//     * Expand or collapse the video fragment animation
//     */
//    override fun setPlayerExpanded(isExpanded: Boolean) {
//
//    }
//
//    private fun expandPlayerTo(isExpanded: Boolean, expandedState: ViewMode) {
//        binding.mainPlayer.alpha = 1f
//        binding.main.updateParams(constraintSet) {
//            setGuidelinePercent(binding.guidelineHorizontal.id, if (isExpanded) 0F else VideoTouchHandler.MIN_VERTICAL_LIMIT)
//            setGuidelinePercent(binding.guidelineVertical.id, if (isExpanded) 0F else VideoTouchHandler.MIN_HORIZONTAL_LIMIT)
//            setGuidelinePercent(binding.guidelineBottom.id, if (isExpanded) 1F else VideoTouchHandler.MIN_BOTTOM_LIMIT)
//            setGuidelinePercent(binding.guidelineMarginEnd.id, if (isExpanded) 1F else VideoTouchHandler.MIN_MARGIN_END_LIMIT)
//            setAlpha(binding.mainDetails.id, if (isExpanded) 1.0F else 0F)
//
//            TransitionManager.beginDelayedTransition(binding.main, ChangeBounds().apply {
//                interpolator = android.view.animation.AnticipateOvershootInterpolator(1.0f)
//                duration = 250
//            })
//        }
//    }
//
//    /**
//     * Show dismiss animation when user have moved
//     * more than 50% horizontally
//     */
//    private fun dismiss() {
//        binding.main.updateParams(constraintSet) {
//            setGuidelinePercent(binding.guidelineVertical.id, VideoTouchHandler.MIN_HORIZONTAL_LIMIT - VideoTouchHandler.MIN_MARGIN_END_LIMIT)
//            setGuidelinePercent(binding.guidelineMarginEnd.id, 0F)
//            TransitionManager.beginDelayedTransition(binding.main, TransitionSet()
//                    .addTransition(ChangeBounds())
//                    .addTransition(Fade()).apply {
//                        interpolator = AnticipateOvershootInterpolator(1.0f)
//                        duration = 250
//                        doAfter { removeFragmentByID(binding.mainPlayer.id) }
//                    })
//        }
//    }
//
//    private fun enableFullScreen(isEnabled: Boolean) {
//        binding.root.doOnPreDraw {
//            if (isEnabled) {
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
//                binding.main.updateParams(constraintSet) {
//                    connect(binding.mainPlayer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//                    connect(binding.mainPlayer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//                    connect(binding.mainPlayer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//                    connect(binding.mainPlayer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//                    setDimensionRatio(binding.mainPlayer.id, "")
//                }
//            } else {
//                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//                binding.main.updateParams(constraintSet) {
//                    connect(binding.mainPlayer.id, ConstraintSet.START, binding.guidelineVertical.id, ConstraintSet.START)
//                    connect(binding.mainPlayer.id, ConstraintSet.END, binding.guidelineMarginEnd.id, ConstraintSet.END)
//                    connect(binding.mainPlayer.id, ConstraintSet.TOP, binding.guidelineHorizontal.id, ConstraintSet.BOTTOM)
//                    clear(binding.mainPlayer.id, ConstraintSet.BOTTOM)
//                    setDimensionRatio(binding.mainPlayer.id, currentPlayerRatio)
//                }
//            }
//            animationTouchListener.pinchToZoomEnabled = isEnabled
//        }
//    }
//
//    private fun displaySubCount(subCount: Int) {
//        binding.mainDrawer.getHeaderView(0).apply {
//            findViewById<TextView>(R.id.header_subtitle).text = getString(R.string.home_subtitle_suffix, subCount)
//        }
//    }
//
//    private fun displayUser(user: User) {
//        binding.mainDrawer.getHeaderView(0).apply {
//            user.with { user ->
//                findViewById<TextView>(R.id.header_title).text = user.entity.username
//                subscriptions += GlideApp.with(this).asBitmap().load(user.entity.profileImage)
//                        .toPalette().subscribe { palette ->
//                            findViewById<ImageView>(R.id.header_icon).setImageBitmap(palette.bitmap)
//                            themeManager.getVibrantSwatch(palette.palette).apply {
//                                findViewById<ImageView>(R.id.header).setBackgroundColor(rgb)
//                                header_title.setTextColor(titleTextColor)
//                                header_subtitle.setTextColor(bodyTextColor)
//                            }
//                        }
//            }
//        }
//    }
//
////    private fun checkForVideoToPlay(): (Observable<MainContract.Action>) -> Observable<MainContract.Action> {
////        return {
////            if (intent.hasExtra(VideoToPlayKey)) {
////                val id = intent.getStringExtra(VideoToPlayKey)!!
////                intent.removeExtra(VideoToPlayKey)
////                it.startWith(MainContract.Action.PlayVideo(id))
////            } else
////                it
////        }
////    }
//
//    companion object {
//        private const val VideoToPlayKey = "VideoToPlay"
//
//        fun navigateTo(context: Context) {
//            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//        }
//
//        fun playVideo(context: Context, videoId: String) = context.startActivity(Intent(context, MainActivity::class.java)
//                .putExtra(VideoToPlayKey, videoId).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//    }
}
