package me.mauricee.pontoon.ui.login

interface LoginNavigator {
    fun onSuccessfulLogin()
    fun toDiscordLogin()
    fun toLttLogin()
    fun toSignUp()
    fun toPrivacyPolicy()
}