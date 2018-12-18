package me.mauricee.pontoon.model.source

import android.content.SharedPreferences
import androidx.core.content.edit
import org.threeten.bp.Instant
import org.threeten.bp.Period
import org.threeten.bp.temporal.ChronoUnit

class AcknowledgementHelper(private val triesBeforeGivingUp: Int = 3, private val key: String,
                            private val sharedPreferences: SharedPreferences,
                            private val daysBeforeNextAsk: Long = 7) {

    var numberOfTries: Int = sharedPreferences.getInt(NumberOfTries, 0)
        set(value) {
            if (field != value) {
                field = value
                sharedPreferences.edit(true) { putInt(NumberOfTries, field) }
                if (field >= triesBeforeGivingUp) {
                    shouldDisplayPrompt = false
                }
            }
        }

    private var lastPrompt = Instant.ofEpochMilli(sharedPreferences.getLong(TimeUntilNextAsk, Instant.now().epochSecond))
        set(value) {
            sharedPreferences.edit(true) { putLong(TimeUntilNextAsk, value.toEpochMilli()) }
        }

    //TODO add a time debounce
    var shouldDisplayPrompt: Boolean = sharedPreferences.getBoolean(ShouldDisplayPrompt, true)
        get() = sharedPreferences.getBoolean(ShouldDisplayPrompt, true) &&
                ChronoUnit.DAYS.between(Instant.now(), lastPrompt) >= daysBeforeNextAsk
        private set(value) {
            if (field != value) {
                sharedPreferences.edit(true) { putBoolean(ShouldDisplayPrompt, field) }
            }
        }

    fun userSaid(acknowledgement: Acknowledgement): Unit = when (acknowledgement) {
        Acknowledgement.Positive -> shouldDisplayPrompt = false
        Acknowledgement.Negative -> numberOfTries += 1
        Acknowledgement.Never -> shouldDisplayPrompt = false
    }

    companion object {
        private const val TimeUntilNextAsk = "numberOfTries"
        private const val NumberOfTries = "numberOfTries"
        private const val ShouldDisplayPrompt = "shouldDisplayPrompt"
    }
}