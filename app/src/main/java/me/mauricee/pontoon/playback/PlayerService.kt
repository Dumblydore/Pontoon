package me.mauricee.pontoon.playback

import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.media2.SessionPlayerConnector
import dagger.android.AndroidInjection
import me.mauricee.pontoon.rx.Optional
import java.util.concurrent.Executors
import javax.inject.Inject

//TODO Implement Downloads
//TODO Implement Quality Levels
//TODO Implement Cast behavior
class PlayerService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        TODO("Not yet implemented")
    }

}