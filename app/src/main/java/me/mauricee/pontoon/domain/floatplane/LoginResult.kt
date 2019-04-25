package me.mauricee.pontoon.domain.floatplane

import me.mauricee.pontoon.domain.floatplane.api.User


sealed class LoginResult {
    object InvalidCredentials : LoginResult()
    object Request2FA : LoginResult()
    data class LoggedIn(val user: User): LoginResult()
    data class Error(val error: Errors = Errors.Unknown): LoginResult()
}