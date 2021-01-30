package me.mauricee.pontoon.model.session

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

class SessionCredentialsSerializer @Inject constructor() : Serializer<SessionCredentials> {
    override val defaultValue: SessionCredentials
        get() = SessionCredentials(cfduid = UUID.randomUUID().toString())

    override fun readFrom(input: InputStream): SessionCredentials {
        return SessionCredentials.ADAPTER.decode(input)
    }

    override fun writeTo(t: SessionCredentials, output: OutputStream) {
        SessionCredentials.ADAPTER.encode(output, t)
    }
}