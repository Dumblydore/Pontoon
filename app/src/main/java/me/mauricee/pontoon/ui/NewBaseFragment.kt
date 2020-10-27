package me.mauricee.pontoon.ui

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import java.util.*

abstract class NewBaseFragment(@LayoutRes layoutId: Int) : DaggerFragment(layoutId) {

    protected val subscriptions = CompositeDisposable()
    protected val animations: List<Animator> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscriptions.clear()
        for (animator in animations) {
            animator.cancel()
        }
    }

    /**
     * Method to reset the state of the fragment. e.g: scroll back to top of a list.
     */
    fun reset() {}
    protected val toolbar: Toolbar?
        protected get() = null
}