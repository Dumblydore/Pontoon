package me.mauricee.pontoon.model.video

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.Module
import dagger.Provides
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.model.PontoonDatabase

@Module
class VideoModelModule {

    @Provides
    fun PontoonDatabase.providesVideoDao(): VideoDao = videoDao

    @Provides
    fun PontoonDatabase.providesRelatedVideoDao(): RelatedVideoDao = relatedVideoDao

    @Provides
    fun providesVideoStore(api: FloatPlaneApi, persistor: VideoPersistor): StoreRoom<Video, String> = StoreRoom.from({ api.getVideo(it) }, persistor)

    @Provides
    fun providesRelatedVideoStore(api: FloatPlaneApi, persistor: RelatedVideoPersistor): StoreRoom<List<Video>, String> = StoreRoom.from({ api.getRelatedVideos(it) }, persistor)
}