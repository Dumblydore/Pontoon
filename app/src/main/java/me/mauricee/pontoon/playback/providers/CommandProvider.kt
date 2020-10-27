package me.mauricee.pontoon.playback.providers

import android.os.Bundle
import androidx.media2.session.MediaSession
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommandGroup
import androidx.media2.session.SessionResult
import com.google.android.exoplayer2.ext.media2.SessionCallbackBuilder
import javax.inject.Inject

class CommandProvider @Inject constructor() : SessionCallbackBuilder.CustomCommandProvider, SessionCallbackBuilder.AllowedCommandProvider {
    override fun onCustomCommand(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, customCommand: SessionCommand, args: Bundle?): SessionResult {
        TODO("Not yet implemented")
    }

    override fun getCustomCommands(session: MediaSession, controllerInfo: MediaSession.ControllerInfo): SessionCommandGroup? {
        TODO("Not yet implemented")
    }

    override fun acceptConnection(session: MediaSession, controllerInfo: MediaSession.ControllerInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAllowedCommands(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, baseAllowedSessionCommand: SessionCommandGroup): SessionCommandGroup {
        TODO("Not yet implemented")
    }

    override fun onCommandRequest(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, command: SessionCommand): Int {
        TODO("Not yet implemented")
    }
}