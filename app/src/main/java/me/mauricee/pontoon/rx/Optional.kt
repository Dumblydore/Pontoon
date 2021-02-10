package me.mauricee.pontoon.rx

import java.lang.RuntimeException

class Optional<T> private constructor(val value: T?) {

    fun requireValue(): T = value ?: throw RuntimeException("Optional value must not be null!")

    companion object {
        fun <T> of(value: T): Optional<T> = Optional(value)
        fun <T> empty(): Optional<T> = Optional(null)
    }
}