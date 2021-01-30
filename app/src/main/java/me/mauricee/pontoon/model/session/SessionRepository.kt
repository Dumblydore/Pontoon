package me.mauricee.pontoon.model.session

import androidx.datastore.core.DataStore
import androidx.datastore.rxjava2.data
import androidx.datastore.rxjava2.updateDataAsync
import io.reactivex.Single
import me.mauricee.pontoon.di.AppScope
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.LoginRequest
import me.mauricee.pontoon.domain.floatplane.UserJson
import javax.inject.Inject

@AppScope
class SessionRepository @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                            private val credentialStore: DataStore<SessionCredentials>) {
    fun canLogin(): Single<Boolean> = credentialStore.data().map {
        it.username.isNotBlank() && it.password.isNotBlank()
    }.first(false)

    fun loginWithStoredCredentials(): Single<LoginResult> = credentialStore.data()
            .map { LoginRequest(it.username, it.password) }
            .flatMapSingle { floatPlaneApi.login(it) }
            .map {
                if (it.needs2Fa) LoginResult.Requires2FA
                else LoginResult.Success
            }.firstOrError().onErrorReturn(LoginResult::Error)

    fun loginWithCredentials(userName: String, password: String) {

    }

    private fun processLogin(user: UserJson.Container): Single<LoginResult> = Single.never()
    private fun storeCredentials(username: String, password: String, user: UserJson) = credentialStore.updateDataAsync { credentials ->
        Single.fromCallable {
            credentials.copy(
                    username = username,
                    password = password
            )
        }
    }

}

sealed class LoginResult {
    object Requires2FA : LoginResult()
    object Success : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}