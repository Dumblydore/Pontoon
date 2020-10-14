package me.mauricee.pontoon.model

import io.reactivex.Observable

data class DataModel<T>(private val cache: Observable<T>, private val refresh: Observable<T>) {

}