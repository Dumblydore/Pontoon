package me.mauricee.pontoon.rx.glide

import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import com.bumptech.glide.RequestBuilder
import io.reactivex.Single

fun RequestBuilder<Bitmap>.toPalette() = toSingle().flatMap { bitmap ->
    Single.fromCallable { Palette.from(bitmap).generate() }.map {
        PaletteSingle.PaletteEvent(bitmap, it)
    }
}

fun RequestBuilder<Bitmap>.toSingle() = Single.fromFuture(submit())