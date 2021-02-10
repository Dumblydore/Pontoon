package me.mauricee.pontoon.rx.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

class BitmapSingle(private val requestBuilder: RequestBuilder<Bitmap>) : Single<Bitmap>() {
    override fun subscribeActual(observer: SingleObserver<in Bitmap>) {
        observer.onSubscribe(Target(observer).also { requestBuilder.into(it) })
    }

    internal class Target(private val observer: SingleObserver<in Bitmap>)
        : CustomTarget<Bitmap>(), Disposable {

        private val isDisposed = AtomicBoolean(false)

        override fun isDisposed(): Boolean = isDisposed.get()

        override fun dispose() {
            isDisposed.set(true)
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

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    }

    internal class BitmapNotAvailable : Exception("Bitmap could not be loaded.")
}