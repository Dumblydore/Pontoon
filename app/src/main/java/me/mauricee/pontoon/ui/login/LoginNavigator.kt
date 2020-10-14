package me.mauricee.pontoon.ui.login

interface LoginNavigator {
    fun onSuccessfulLogin()
    fun promptFor2FA()
    fun toDiscordLogin()
    fun toLttLogin()
    fun toSignUp()
    fun toPrivacyPolicy()
}