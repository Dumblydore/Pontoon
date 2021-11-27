package me.mauricee.pontoon.common.log

import timber.log.Timber

inline fun Any.logd(message: String, exception: Throwable? = null) {
    Timber.d(exception, message)
}

inline fun Any.logi(message: String, exception: Throwable? = null) {
    Timber.i(exception, message)
}

inline fun Any.loge(exception: Throwable) = loge("", exception)

inline fun Any.loge(message: String, exception: Throwable? = null) {
    Timber.e(exception, message)
}

inline fun Any.logw(message: String, exception: Throwable? = null) {
    Timber.w(exception, message)
}

inline fun Any.logwtf(message: String, exception: Throwable? = null) {
    Timber.wtf(exception, message)
}