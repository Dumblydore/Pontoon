package me.mauricee.pontoon.common.log

import timber.log.Timber

fun Any.logd(message: String, exception: Throwable? = null) {
    Timber.d(exception, message)
}

fun Any.logi(message: String, exception: Throwable? = null) {
    Timber.i(exception, message)
}

fun Any.loge(exception: Throwable) = loge("", exception)

fun Any.loge(message: String, exception: Throwable? = null) {
    Timber.e(exception, message)
}

fun Any.logw(message: String, exception: Throwable? = null) {
    Timber.w(exception, message)
}

fun Any.logwtf(message: String, exception: Throwable? = null) {
    Timber.wtf(exception, message)
}