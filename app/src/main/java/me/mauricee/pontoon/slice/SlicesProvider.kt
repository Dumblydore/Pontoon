package me.mauricee.pontoon.slice

import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.ListBuilder
import me.mauricee.pontoon.ext.logd

class SlicesProvider : SliceProvider() {


//    @Inject lateinit var videoRepository: VideoRepository


    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)

    }

    override fun onBindSlice(sliceUri: Uri): Slice? = when (sliceUri.path) {
        "/subscriptions" -> buildSubList(sliceUri)
        else -> null
    }.also { logd("path: $sliceUri") }

    private fun buildSubList(sliceUri: Uri): Slice {
        logd("buildingSublist")
        return ListBuilder(context, sliceUri, ListBuilder.INFINITY).also { list ->
            ListBuilder.RowBuilder().apply {
                setTitle("this is a test")

            }.also { list.addRow(it) }
        }.build()
    }

    override fun onCreateSliceProvider(): Boolean {
//        DaggerSliceComponent.builder()
//                .appComponent((context.applicationContext as Pontoon).appComponent).build()
//                .inject(this)
        return true
    }
}