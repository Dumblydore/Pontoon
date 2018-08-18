package me.mauricee.pontoon.rx.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.reactivex.disposables.Disposable

class NetworkStateObservable(private val connectivityManager: ConnectivityManager) {

    init {

    }

    private inner class Listener : ConnectivityManager.NetworkCallback(), Disposable {

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)


        }

        override fun isDisposed(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun dispose() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}