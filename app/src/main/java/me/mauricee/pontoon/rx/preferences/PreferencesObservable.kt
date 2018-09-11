package me.mauricee.pontoon.rx.preferences

import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

class PreferencesObservable(private val key: String, private val preferences: SharedPreferences) : Observable<SharedPreferences>() {
    override fun subscribeActual(observer: Observer<in SharedPreferences>) {
        Listener(key, observer).also {
            preferences.registerOnSharedPreferenceChangeListener(it)
            observer.onSubscribe(it)
            if (preferences.contains(key)) observer.onNext(preferences)
        }
    }

    inner class Listener(private val key: String, private val observer: Observer<in SharedPreferences>) :
            MainThreadDisposable(), SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onDispose() {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            verifyMainThread()
            if (!isDisposed && key == this.key)
                observer.onNext(sharedPreferences)
        }

    }
}