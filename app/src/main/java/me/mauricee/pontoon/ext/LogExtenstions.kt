package me.mauricee.pontoon.ext

import android.util.Log

fun Any.logd(message: String, exception: Throwable? = null) {
    Log.d(this.javaClass.simpleName, message, exception)
}

fun Any.logi(message: String, exception: Throwable? = null) {
    Log.i(this.javaClass.simpleName, message, exception)
}

fun Any.loge(exception: Throwable) = loge("", exception)

fun Any.loge(message: String, exception: Throwable? = null) {
    Log.e(this.javaClass.simpleName, message, exception)
}

fun Any.logw(message: String, exception: Throwable? = null) {
    Log.w(this.javaClass.simpleName, message, exception)
}

fun Any.logwtf(message: String, exception: Throwable? = null) {
    Log.wtf(this.javaClass.simpleName, message, exception)
}