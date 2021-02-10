package me.mauricee.pontoon.ui

import android.animation.Animator
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    protected val subscriptions = CompositeDisposable()
    protected val animations = mutableListOf<Animator>()

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
        for (animator in animations) {
            animator.cancel()
        }
    }
}