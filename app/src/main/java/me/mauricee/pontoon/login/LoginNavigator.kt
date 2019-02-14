package me.mauricee.pontoon.login

interface LoginNavigator {
    fun onSuccessfulLogin()
    fun toDiscordLogin()
    fun toLttLogin()
    fun toSignUp()
    fun toPrivacyPolicy()
}