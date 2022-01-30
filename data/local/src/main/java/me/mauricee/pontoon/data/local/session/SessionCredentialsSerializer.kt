package me.mauricee.pontoon.data.local.session

import androidx.datastore.core.Serializer
import me.mauricee.pontoon.model.session.SessionCredentials
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object SessionCredentialsSerializer : Serializer<SessionCredentials> {
    override val defaultValue: SessionCredentials
        get() = SessionCredentials(cfuId = UUID.randomUUID().toString())

    override suspend fun readFrom(input: InputStream): SessionCredentials {
        return SessionCredentials.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: SessionCredentials, output: OutputStream) {
        SessionCredentials.ADAPTER.encode(output, t)
    }
}