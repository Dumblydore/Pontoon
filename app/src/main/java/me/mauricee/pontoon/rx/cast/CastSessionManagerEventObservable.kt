package me.mauricee.pontoon.rx.cast

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

internal class CastSessionManagerEventObservable(private val sessionManager: SessionManager) : Observable<SessionEvent>() {
    override fun subscribeActual(observer: Observer<in SessionEvent>) {
        observer.onSubscribe(Listener(observer, sessionManager))
    }

    inner class Listener(private val observer: Observer<in SessionEvent>,
                         private val sessionManager: SessionManager) : Disposable, SessionManagerListener<CastSession> {
        private val disposed = AtomicBoolean(false)

        init {
            sessionManager.addSessionManagerListener(this, CastSession::class.java)
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            if (!isDisposed)
                observer.onNext(SessionEvent.ConnectedEvent.Started(session))
        }

        override fun onSessionResumeFailed(session: CastSession, errorCode: Int) {
            if (!isDisposed)
                observer.onNext(SessionEvent.DisconnectedEvent.ResumeFailed)
        }

        override fun onSessionSuspended(session: CastSession, errorCode: Int) {
            if (!isDisposed)
                observer.onNext(SessionEvent.Suspended)
        }

        override fun onSessionEnded(session: CastSession, errorCode: Int) {
            if (!isDisposed)
                observer.onNext(SessionEvent.DisconnectedEvent.Ended)
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            if (!isDisposed)
                observer.onNext(SessionEvent.ConnectedEvent.Resumed(session))
        }

        override fun onSessionStarting(session: CastSession) {
            if (!isDisposed)
                observer.onNext(SessionEvent.Starting)
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            if (!isDisposed)
                observer.onNext(SessionEvent.Resuming(session))
        }

        override fun onSessionEnding(session: CastSession) {
            if (!isDisposed)
                observer.onNext(SessionEvent.Ending)
        }

        override fun onSessionStartFailed(session: CastSession, errorCode: Int) {
            if (!isDisposed)
                observer.onNext(SessionEvent.DisconnectedEvent.StartFailed)
        }

        override fun isDisposed(): Boolean = disposed.get()

        override fun dispose() {
            sessionManager.removeSessionManagerListener(this, CastSession::class.java)
            disposed.set(true)
        }
    }
}

sealed class SessionEvent {
    sealed class ConnectedEvent(val castSession: CastSession) : SessionEvent() {
        class Started(castSession: CastSession) : ConnectedEvent(castSession)
        class Resumed(castSession: CastSession) : ConnectedEvent(castSession)
    }

    sealed class DisconnectedEvent : SessionEvent() {
        object Ended : DisconnectedEvent()
        object StartFailed : DisconnectedEvent()
        object ResumeFailed : DisconnectedEvent()
    }

    object Starting : SessionEvent()
    class Resuming(val castSession: CastSession) : SessionEvent()
    object Suspended : SessionEvent()
    object Ending : SessionEvent()


    fun isConnected() = this is ConnectedEvent
    fun isDisconnected() = this is DisconnectedEvent
}

