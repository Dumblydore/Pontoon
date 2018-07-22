package me.mauricee.pontoon.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import me.mauricee.pontoon.Pontoon
import java.io.InputStream


@GlideModule
class PontoonGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val factory = OkHttpUrlLoader.Factory((context.applicationContext as Pontoon).client)
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}

