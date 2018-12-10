package me.mauricee.pontoon.common.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import me.mauricee.pontoon.R

class OptionProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.cast_id))
                .build()
    }

    override fun getAdditionalSessionProviders(context: Context): MutableList<SessionProvider> = mutableListOf()
}