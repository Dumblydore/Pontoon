package me.mauricee.pontoon.domain.floatplane

import dagger.Lazy
import io.reactivex.Single
import me.mauricee.pontoon.domain.floatplane.api.FloatPlaneApi
import me.mauricee.pontoon.domain.floatplane.api.LoginAuthToken
import me.mauricee.pontoon.domain.floatplane.api.LoginRequest
import me.mauricee.pontoon.domain.floatplane.api.User
import retrofit2.HttpException
import javax.inject.Inject

class FloatplaneClient @Inject constructor(private val apiProvider: Lazy<FloatPlaneApi>) {
    private val floatplaneApi: FloatPlaneApi by lazy { apiProvider.get() }

    fun login(username: String, password: String): Single<LoginResult> = floatplaneApi.login(LoginRequest(username, password))
            .map(this::processLoginResult)
            .onErrorReturn(this::processLoginError)

    fun check2FaLogin(code: String): Single<LoginResult> =
            floatplaneApi.login(LoginAuthToken(code))
                    .map(this::processLoginResult)
                    .onErrorReturn(this::processLoginError)

    private fun processLoginResult(userContainer: User.Container) = if (userContainer.needs2Fa) LoginResult.Request2FA
    else userContainer.user?.let(LoginResult::LoggedIn) ?: LoginResult.Error()

    private fun processLoginError(error: Throwable): LoginResult = when (error) {
        is HttpException -> processHttpCode(error.code())
        else -> LoginResult.Error()
    }

    private fun processHttpCode(code: Int): LoginResult = when (code) {
        in 400..499 -> LoginResult.InvalidCredentials
        in 500..599 -> LoginResult.Error(Errors.Service)
        else -> LoginResult.Error()
    }
}