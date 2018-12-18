package me.mauricee.pontoon.model.source

import android.net.Uri
import io.reactivex.Single

/** interface for facilitating ratings, updates, etc. for apks from different sources (Github vs. Play store)*/
interface SourceManager {
    fun shouldPromptForRating(): Single<Boolean>
    fun isMostRecentVersion(): Single<Boolean>
    fun getProductUrl(): Single<Uri>
    fun userAcknowledgedReviewPrompt(acknowledgement: Acknowledgement)
    fun userAcknowledgedUpdatePrompt(acknowledgement: Acknowledgement)
}