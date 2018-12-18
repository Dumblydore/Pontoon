package me.mauricee.pontoon.login

interface LoginNavigator {
    fun onSuccessfulLogin()
    fun toLttLogin()
    fun toDiscord()
    fun toSignup()
}