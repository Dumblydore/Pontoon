package me.mauricee.pontoon.rx.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

class BitmapSingle(private val requestBuilder: RequestBuilder<Bitmap>) : Single<Bitmap>() {
    override fun subscribeActual(observer: SingleObserver<in Bitmap>) {
        observer.onSubscribe(Target(observer).also { requestBuilder.into(it) })
    }

    internal class Target(private val observer: SingleObserver<in Bitmap>)
        : SimpleTarget<Bitmap>(), Disposable {

        private var runningTask: AsyncTask<Bitmap, Void, Bitmap>? = null

        override fun isDisposed(): Boolean = runningTask?.isCancelled == false

        override fun dispose() {
            runningTask?.cancel(true)
        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            observer.onSuccess(resource)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            if (errorDrawable != null)
                observer.onSuccess(errorDrawable.toBitmap())
            else
                observer.onError(BitmapNotAvailable())
        }
    }

    internal class BitmapNotAvailable : Exception("Bitmap could not be loaded.")
}