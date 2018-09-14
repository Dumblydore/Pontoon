package me.mauricee.pontoon.common

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.lazy_error_layout.view.*
import me.mauricee.pontoon.R

/**
 * A layout view that simplifies loading, error, and success states of a view
 */
class LazyLayout : FrameLayout {

    companion object {
        private const val DEFAULT_ANIMATION_DURATION: Long = 250
        const val ERROR = -1
        const val LOADING = 0
        const val SUCCESS = 1
    }

    var displayRetryButton = true
        set(value) {
            lazy_error_retry?.isVisible = value
        }

    private var loadingView: View? = null
        set(value) {
            field?.also(::removeView)
            if (value !is SwipeRefreshLayout)
                value?.also(::addView)
            field = value
        }
    var errorView: View? = null
        set(value) {
            field?.also(::removeView)
            value?.also(::addView)
            field = value
        }

    var errorText: String? = null
        set(value) {
            lazy_error_text?.text = value
        }
    private var successView: View? = null
//        set(value) {
//            field?.also(::removeView)
//            value?.also(::addView)
//            field = value
//        }


    private var stateUpdateListener: StateUpdateListener? = null
    var retryListener: RetryListener? = null

    @State
    var state = LOADING
        set(value) {
            if (field != value) {
                field = value
                setState(value)
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)

    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LazyLayout)
        @LayoutRes var loadingLayout = R.layout.lazy_loading_layout
        @LayoutRes var errorLayout = R.layout.lazy_error_layout

        loadingLayout = a.getResourceId(R.styleable.LazyLayout_loading_layout, loadingLayout)
        errorLayout = a.getResourceId(R.styleable.LazyLayout_error_layout, errorLayout)
        displayRetryButton = a.getBoolean(R.styleable.LazyLayout_display_retry, false)

        loadingView = LayoutInflater.from(context).inflate(loadingLayout, this, false)
        errorView = LayoutInflater.from(context).inflate(errorLayout, this, false)
        //Lol

        state = a.getInt(R.styleable.LazyLayout_lazy_state, LOADING)
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 3) {
            throw RuntimeException("This layout requires one child!")
        }

        successView = getChildAt(2)
        loadingView?.visibility = View.GONE
        errorView?.visibility = View.GONE

        successView?.visibility = View.GONE

        lazy_error_retry?.setOnClickListener { retryListener?.onRetry() }
        updateViewState()
    }

    fun setErrorView(@LayoutRes viewId: Int) {
        errorView = LayoutInflater.from(context).inflate(viewId, this, false)
    }

    fun setState(@State state: Int, animate: Boolean) = setState(state, animate, true)

    @Synchronized
    private fun setState(@State state: Int, animate: Boolean = true, triggerNotify: Boolean = true) {
        postDelayed(DEFAULT_ANIMATION_DURATION) {
            when {
                loadingView is SwipeRefreshLayout -> updateSwipeRefreshViewState()
                animate -> animateViewState()
                else -> updateViewState()
            }

            if (triggerNotify) {
                stateUpdateListener?.onStateUpdated(state)
            }
        }
    }

    fun setStateChangeListener(listener: StateUpdateListener?) {
        this.stateUpdateListener = listener
    }

    fun setupWithSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout) {
        loadingView = swipeRefreshLayout
        //TODO Do this later
//        swipeRefreshLayout.setOnRefreshListener { state = LOADING }
    }

    private fun animateViewState() {
        errorView?.animate()?.cancel()
        loadingView?.animate()?.cancel()
        successView?.animate()?.cancel()

        val newActiveView: View = (
                when (state) {
                    ERROR -> errorView
                    LOADING -> loadingView
                    SUCCESS -> successView
                    else -> throw RuntimeException("Invalid LazyLayout.State")
                }) as View
        children().firstOrNull(View::isVisible)?.let {
            it.alpha = 1f
            getInactiveAnimation(it, getActiveAnimation(newActiveView))
        }?.start()
    }

    private fun updateViewState() {
        val newActiveView: View = (
                when (state) {
                    ERROR -> errorView
                    LOADING -> loadingView
                    SUCCESS -> successView
                    else -> throw RuntimeException("Invalid LazyLayout.State")
                }) as View

        children().filter { i -> newActiveView != i }.forEach { i -> i.visibility = View.GONE }
        newActiveView.visibility = View.VISIBLE
        newActiveView.bringToFront()
    }

    private fun updateSwipeRefreshViewState() {
        val loadingView = this.loadingView as? SwipeRefreshLayout
        loadingView?.isRefreshing = state == LOADING
        if (state == SUCCESS) {
            getInactiveAnimation(errorView!!, getActiveAnimation(successView!!)).start()
        } else if (state == ERROR) {
            getInactiveAnimation(successView!!, getActiveAnimation(errorView!!)).start()
        }
    }

    private fun getActiveAnimation(view: View): ViewPropertyAnimator {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.bringToFront()
        return view.animate().alpha(1f)
                .setStartDelay(DEFAULT_ANIMATION_DURATION)
                .setDuration(DEFAULT_ANIMATION_DURATION)
    }

    private fun getInactiveAnimation(view: View,
                                     activeAnimation: ViewPropertyAnimator): ViewPropertyAnimator {
        return view.animate().alpha(0f)
                .setStartDelay(DEFAULT_ANIMATION_DURATION)
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .withEndAction {
                    view.visibility = View.GONE
                    activeAnimation.start()
                }
    }

    private fun children(): List<View> {
        return IntRange(0, childCount - 1).map(this::getChildAt)
    }

    @IntDef(ERROR, LOADING, SUCCESS)
    @Retention
    private annotation class State

    interface StateUpdateListener {
        fun onStateUpdated(@State state: Int)
    }

    interface RetryListener {
        fun onRetry()
    }

}
