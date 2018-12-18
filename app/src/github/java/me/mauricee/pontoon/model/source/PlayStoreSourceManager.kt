package me.mauricee.pontoon.model.source

import android.content.SharedPreferences
import android.net.Uri
import io.reactivex.Single
import javax.inject.Inject

class PlayStoreSourceManager @Inject constructor(private val sharedPreferences: SharedPreferences): SourceManager {

    override fun shouldPromptForRating(): Single<Boolean> = Single.just(true)

    override fun isMostRecentVersion(): Single<Boolean> = Single.just(true)

    override fun getProductUrl(): Single<Uri> = Single.just(Uri.EMPTY)
}