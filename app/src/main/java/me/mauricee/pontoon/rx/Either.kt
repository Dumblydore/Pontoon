package me.mauricee.pontoon.rx

data class Either<A, B>(val value: A?, val otherValue: B?) {
    fun either(action: (A) -> Unit): Either<A, B> {
        value?.let(action)
        return this
    }

    fun or(action: (B) -> Unit): Either<A, B> {
        otherValue?.let(action)
        return this
    }

    companion object {
        fun <A, B> either(value: A): Either<A, B> = Either(value, null)
        fun <A, B> or(value: B): Either<A, B> = Either(null, value)
    }
}