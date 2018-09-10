package me.mauricee.pontoon.rx.preferences

import android.content.SharedPreferences
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable

fun SharedPreferences.watchBoolean(key: String): Observable<Boolean> =
        PreferencesObservable(key, this).map { it.getBoolean(key, false) }

fun SharedPreferences.watchInt(key: String): Observable<Int> =
        PreferencesObservable(key, this).map { it.getInt(key, -1) }

fun SharedPreferences.watchAll(): Observable<String> =
        Observable.just(this.all).flatMap { it.keys.toObservable() }
                .flatMap { key -> PreferencesObservable(key, this).map { key } }

fun SharedPreferences.watchLong(key: String): Observable<Long> =
        PreferencesObservable(key, this).map { it.getLong(key, -1L) }

fun SharedPreferences.watchFloat(key: String): Observable<Float> =
        PreferencesObservable(key, this).map { it.getFloat(key, -1f) }

fun SharedPreferences.watchStringSet(key: String, defaultValue: MutableSet<String> = mutableSetOf()): Observable<MutableSet<String>> =
        PreferencesObservable(key, this).map { it.getStringSet(key, defaultValue) }

fun SharedPreferences.watchString(key: String): Observable<String> =
        PreferencesObservable(key, this).map { it.getString(key, "") }
