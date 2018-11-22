package me.mauricee.pontoon.ext

inline fun <T> T.just(crossinline block: T.() -> Unit): Unit = block()
