package me.mauricee.pontoon.rx.cast

import com.google.android.gms.cast.framework.SessionManager
import io.reactivex.Observable

fun SessionManager.events(): Observable<SessionEvent> = CastSessionManagerEventObservable(this)
        .distinctUntilChanged { t1: SessionEvent, t2: SessionEvent -> t1.isConnected() && t2.isConnected() }
        .distinctUntilChanged { t1: SessionEvent, t2: SessionEvent -> t1.isDisconnected() && t2.isDisconnected() }