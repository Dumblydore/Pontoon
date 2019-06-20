package me.mauricee.pontoon.common.gestures

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dagger.Reusable
import io.reactivex.Observable
import me.mauricee.pontoon.ext.logd
import javax.inject.Inject

/**
 * Created by Burhanuddin Rashid on 2/25/2018.
 *
 * This class is fully dependent on Dashboard activity layout it will perform youtube like
 * animation on Framelayout onTouchListener.The logic for animation resides here to make
 * it modular
 */

@Reusable
class VideoTouchHandler @Inject constructor(private val activity: AppCompatActivity) : View.OnTouchListener {

    private val eventRelay: Relay<GestureEvent> = PublishRelay.create()

    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels//activity.getDeviceHeight()
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels//activity.getDeviceWidth()

    //Gesture controls and scroll flags
    private var gestureDetector = GestureDetector(activity, GestureControl())
    private var scaleGestureDetector = ScaleGestureDetector(activity, ScaleControl())
    private var isTopScroll = false
    private var isSwipeScroll = false

    //Initialize touch variables
    private var startX = 0F
    private var startY = 0F
    private var dX = 0F
    private var dY = 0F
    private var percentVertical = 0F
    private var percentMarginMoved = MIN_MARGIN_END_LIMIT

    val events: Observable<GestureEvent>
        get() = eventRelay.hide()
    var isEnabled = true
    var pinchToZoomEnabled = false
    var isSnackbarShowing = false
    var minVerticalLimit: Float = MIN_VERTICAL_LIMIT
        private set

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            eventRelay.accept(GestureEvent.Click(view))
            //return only when player is more than threshold value i.e is already expanded
            if (percentVertical > SCALE_THRESHOLD) return true
        }

        if (pinchToZoomEnabled) {
            scaleGestureDetector.onTouchEvent(event)
            if (scaleGestureDetector.isInProgress) {
                eventRelay.accept(GestureEvent.Scale(scaleGestureDetector.scaleFactor))
            }
            return true
        }


        if (!isEnabled) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                dX = view.x - startX
                dY = view.y - startY

            }

            MotionEvent.ACTION_MOVE -> {
                percentVertical = (event.rawY + dY) / deviceHeight.toFloat()
                val percentHorizontal = (event.rawX + dX) / deviceWidth.toFloat()

                when (getDirection(startX = startX, startY = startY, endX = event.rawX, endY = event.rawY)) {
                    is Direction.Left, is Direction.Right -> {

                        //Don't perform swipe if video frame is expanded or scrolling up/down
                        if (!(!isTopScroll && !isExpanded)) return false

                        //set swipe flag to avoid up/down scroll
                        isSwipeScroll = true
                        //Prevent guidelines to go out of screen bound
                        val percentHorizontalMoved = Math.max(-0.25F, Math.min(MIN_HORIZONTAL_LIMIT, percentHorizontal))
                        percentMarginMoved = percentHorizontalMoved + (MIN_MARGIN_END_LIMIT - MIN_HORIZONTAL_LIMIT)
                        eventRelay.accept(GestureEvent.Swipe(percentHorizontal))
                        //swipeVideo(percentHorizontal)
                    }
                    is Direction.Up, is Direction.Down, is Direction.None -> {

                        //Don't expand video when user is swiping the video when its not expanded
                        if (isSwipeScroll) return false

                        //set up/down flag to avoid swipe scroll
                        isTopScroll = true
                        eventRelay.accept(GestureEvent.Scale(percentVertical))
                        //scaleVideo(percentVertical)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                isTopScroll = false
                isSwipeScroll = false
                if (percentMarginMoved < 0.5) {
                    //dismiss the video player
                    eventRelay.accept(GestureEvent.Dismiss(view))
                    resetValues()
                } else {
                    isExpanded = percentVertical < SCALE_THRESHOLD
                }
            }
        }
        return true
    }

    //Setup direction types from Direction sealed class
    private val left: Direction = Direction.Left
    private val right: Direction = Direction.Right
    private val up: Direction = Direction.Up
    private val down: Direction = Direction.Down
    private val none: Direction = Direction.None


    /**
     * return a Direction on which user is current scrolling by getting
     * start event coordinates when user press down and end event coordinates when user
     * moves the finger on view
     */
    private fun getDirection(startX: Float, startY: Float, endX: Float, endY: Float): Direction {
        val deltaX = endX - startX
        val deltaY = endY - startY

        return if (Math.abs(deltaX) > Math.abs(deltaY)) {
            //Scrolling Horizontal
            if (deltaX > 0) right else left
        } else {
            //Scrolling Vertical
            if (Math.abs(deltaY) > SWIPE_MIN_DISTANCE) {
                if (deltaY > 0) down else up
            } else {
                none
            }
        }
    }


    var isExpanded = true
        set(value) {
            field = value
            eventRelay.accept(GestureEvent.Expand(field))
        }


    fun show() {
        if (!isExpanded) {
            isExpanded = true
        }
    }

    fun setMinVerticalLimit(playercontainer: View, vararg views: View) {
        val combinedHeight = if (isExpanded) playercontainer.measuredHeight.toFloat() else playercontainer.height * (1 - scaleGestureDetector.scaleFactor)
                + views.sumBy(View::getHeight).toFloat() + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, activity.resources.displayMetrics)
                .toInt()

        val containerHeight = activity.window.decorView.findViewById<View>(android.R.id.content).height.toFloat()
        minVerticalLimit = 1 - (combinedHeight / containerHeight)
        logd("$combinedHeight/$containerHeight = $minVerticalLimit")

    }

    private fun resetValues() {
        isTopScroll = false
        isSwipeScroll = false

        //Initialize touch variables
        startX = 0F
        startY = 0F
        dX = 0F
        dY = 0F
        percentVertical = 0F
        percentMarginMoved = MIN_MARGIN_END_LIMIT
    }


    companion object {
        val TAG = VideoTouchHandler::class.java.simpleName
        /**
         * Video limit params set minimum size a video can scale from both vertical
         * and horizontal directions
         */
        private const val MIN_VERTICAL_LIMIT = 0.685F
        const val MIN_HORIZONTAL_LIMIT = 0.425F
        const val MIN_BOTTOM_LIMIT = 0.90F
        const val MIN_MARGIN_END_LIMIT = 0.975F

        /**
         * Define a threshold value to which when view moves above that threshold when
         * touch action is up than automatically scale to top else scale to the
         * minimum size
         */
        const val SCALE_THRESHOLD = 0.35F
        const val SWIPE_MIN_DISTANCE = 120

    }
}