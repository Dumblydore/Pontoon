package me.mauricee.pontoon.ext

inline fun <T> T.just(crossinline block: T.() -> Unit): Unit = block()
inline fun <T> T.with(crossinline block: (it: T) -> Unit): Unit = block(this)

class NumberUtil {
    companion object {
        fun gcm(a: Long, b: Long): Long = if (b == 0L) a else gcm(b, a % b)
        fun asFraction(a: Long, b: Long, delimitter: String = "/"): String = gcm(a, b).let { "${a / it}$delimitter${b / it}" }
    }
}
