package me.mauricee.pontoon.rx.glide

import android.graphics.Bitmap
import com.bumptech.glide.RequestBuilder

fun RequestBuilder<Bitmap>.toPalette() = PaletteSingle(this)
fun RequestBuilder<Bitmap>.toSingle() = BitmapSingle(this)