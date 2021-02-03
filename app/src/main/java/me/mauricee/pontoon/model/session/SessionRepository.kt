package me.mauricee.pontoon.model.session

import androidx.datastore.core.DataStore
import androidx.datastore.rxjava2.data
import androidx.datastore.rxjava2.updateDataAsync
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleTransformer
import me.mauricee.pontoon.domain.floatplane.*
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                            private val credentialStore: DataStore<SessionCredentials>) {

    fun canLogin(): Single<Boolean> = credentialStore.data().map {
        it.username.isNotBlank() && it.password.isNotBlank()
    }.first(false)

    fun loginWithStoredCredentials(): Single<LoginResult> = credentialStore.data().firstOrError()
            .flatMap { loginWithCredentials(it.username, it.password) }

    fun loginWithCredentials(username: String, password: String): Single<LoginResult> {
        return Single.defer { floatPlaneApi.login(LoginRequest(username, password)) }.compose(processLogin())
    }

    fun loginWithCookie(cfuId: String, sid: String): Single<LoginResult> = credentialStore.updateDataAsync {
        Single.fromCallable { it.copy(cfuId = cfuId, sid = sid) }
    }.flatMap { floatPlaneApi.self.compose(processAuthentication()) }

    fun authenticate(code: String): Single<LoginResult> {
        return floatPlaneApi.login(LoginAuthToken(code)).compose(processLogin())
    }

    fun activate(username: String, activationCode: String): Single<LoginResult> {
        return floatPlaneApi.confirmEmail(ConfirmationRequest(activationCode, username))
                .andThen(floatPlaneApi.self.compose(processAuthentication()))
    }

    fun logout(): Completable = credentialStore.updateDataAsync {
        Single.fromCallable { SessionCredentials() }
    }.ignoreElement().onErrorComplete()

    private fun processLogin(): SingleTransformer<UserJson.Container, LoginResult> = SingleTransformer { stream ->
        stream.map {
            if (it.needs2Fa) LoginResult.Requires2FA
            else LoginResult.Success
        }.onErrorReturn(LoginResult::Error)
    }

    private fun processAuthentication() = SingleTransformer<UserJson, LoginResult> { stream ->
        stream.map<LoginResult> { LoginResult.Success }.onErrorReturn {
            if ((it as? HttpException)?.code() in 400..499) LoginResult.Requires2FA
            else LoginResult.Error(it)
        }
    }

    private fun storeCredentials(username: String, password: String, user: UserJson) = credentialStore.updateDataAsync { credentials ->
        Single.fromCallable { credentials.copy(username = username, password = password) }
    }

}

sealed class LoginResult {
    object Requires2FA : LoginResult()
    object Success : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}