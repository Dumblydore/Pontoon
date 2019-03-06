package me.mauricee.pontoon.rx.glide

import android.graphics.Bitmap
import android.os.AsyncTask
import androidx.palette.graphics.Palette
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class PaletteSingle(private val requestBuilder: RequestBuilder<Bitmap>) : Single<PaletteSingle.PaletteEvent>() {
    override fun subscribeActual(observer: SingleObserver<in PaletteSingle.PaletteEvent>) {
        observer.onSubscribe(Target(observer).also { requestBuilder.into(it) })
    }

    internal class Target(private val observer: SingleObserver<in PaletteSingle.PaletteEvent>)
        : SimpleTarget<Bitmap>(), Palette.PaletteAsyncListener, Disposable {

        private var bitmap: Bitmap? = null
        private var runningTask: AsyncTask<Bitmap, Void, Palette>? = null

        override fun isDisposed(): Boolean = runningTask?.isCancelled == false

        override fun dispose() {
            runningTask?.cancel(true)
            bitmap = null
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            bitmap = resource
            Palette.from(resource).generate(this)
        }

        override fun onGenerated(palette: Palette?) {
            if (palette == null) observer.onError(PaletteNotAvailable()) else observer.onSuccess(PaletteEvent(bitmap!!, palette))
        }
    }

    data class PaletteEvent(val bitmap: Bitmap, val palette: Palette)
    internal class PaletteNotAvailable : Exception("Palette could not be generated with bitmap.")
}