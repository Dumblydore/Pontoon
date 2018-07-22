package me.mauricee.pontoon

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : DaggerAppCompatActivity() {
    internal abstract val fragmentContainer: Int
    internal abstract fun initialFragment(): Fragment
    internal val subscriptions = CompositeDisposable()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
//        activeFragment = initialFragment()
    }

    private fun checkForSharedToolbars(to: Fragment?, transaction: FragmentTransaction) {
        (to as? BaseFragment<*>)?.toolbar?.apply {
            transaction.addSharedElement(this, "toolbar")
        }
    }
}