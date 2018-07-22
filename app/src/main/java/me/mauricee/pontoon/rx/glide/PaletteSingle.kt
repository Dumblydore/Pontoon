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

class PaletteSingle(private val requestBuilder: RequestBuilder<Bitmap>) : Single<Palette>() {
    override fun subscribeActual(observer: SingleObserver<in Palette>) {
        observer.onSubscribe(Target(observer).also { requestBuilder.into(it) })
    }

    internal class Target(private val observer: SingleObserver<in Palette>)
        : SimpleTarget<Bitmap>(), Palette.PaletteAsyncListener, Disposable {

        private var runningTask: AsyncTask<Bitmap, Void, Palette>? = null

        override fun isDisposed(): Boolean = runningTask?.isCancelled == false

        override fun dispose() {
            runningTask?.cancel(true)
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            Palette.from(resource).generate(this)
        }

        override fun onGenerated(palette: Palette?) {
            if (palette == null) observer.onError(PaletteNotAvailable()) else observer.onSuccess(palette)
        }
    }

    internal class PaletteNotAvailable : Exception("Palette could not be generated with bitmap.")
}