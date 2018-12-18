package me.mauricee.pontoon.model.source

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import javax.inject.Inject

class GithubSourceManager @Inject constructor(sharedPreferences: SharedPreferences) : SourceManager {

    override fun shouldPromptForRating(): Single<Boolean> = Single.just(false)

    override fun isMostRecentVersion(): Single<Boolean> = Single.just(false)

    override fun getProductUrl(): Single<Uri> = "https://github.com/Dumblydore/Pontoon/releases".toUri().toSingle()

    override fun userAcknowledgedReviewPrompt(acknowledgement: SourceManager.Acknowledgement) {
        //STUB
    }

    override fun userAcknowledgedUpdatePrompt(acknowledgement: SourceManager.Acknowledgement) = when(acknowledgement) {
        SourceManager.Acknowledgement.Positive -> TODO()
        SourceManager.Acknowledgement.Negative -> TODO()
        SourceManager.Acknowledgement.Never -> TODO()
    }

}