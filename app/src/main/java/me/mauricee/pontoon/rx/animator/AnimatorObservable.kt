package me.mauricee.pontoon.rx.animator

import android.animation.Animator
import android.animation.ValueAnimator
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

internal class AnimatorObservable(private val animator: ValueAnimator) : Observable<ValueAnimator>() {


    override fun subscribeActual(observer: Observer<in ValueAnimator>) {
        val listener = Listener(animator, observer)
        observer.onSubscribe(listener)
        animator.start()
    }

    internal class Listener(private val animator: ValueAnimator,
                            private val observer: Observer<in ValueAnimator>) :
            MainThreadDisposable(), Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

        init {
            animator.addListener(this)
            animator.addUpdateListener(this)
        }

        override fun onDispose() {
            animator.removeUpdateListener(this)
            animator.listeners -= this
        }

        override fun onAnimationRepeat(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            observer.onComplete()
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            observer.onNext(animation)
        }

    }
}