package me.mauricee.pontoon.ext

inline fun <T> T.just(crossinline block: T.() -> Unit): Unit = block()
inline fun <T> T.with(crossinline block: (it: T) -> Unit): Unit = block(this)
