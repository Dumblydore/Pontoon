package me.mauricee.pontoon.model.video

import com.nytimes.android.external.store3.base.impl.StalePolicy
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.model.PontoonDatabase

@Module
@InstallIn(ActivityRetainedComponent::class)
class VideoModelModule {

    @Provides
    fun PontoonDatabase.providesVideoDao(): VideoDao = videoDao

    @Provides
    fun PontoonDatabase.providesRelatedVideoDao(): RelatedVideoDao = relatedVideoDao

    @Provides
    fun providesVideoStore(api: FloatPlaneApi, persistor: VideoPersistor): StoreRoom<Video, String> = StoreRoom.from({ api.getVideo(it) }, persistor, StalePolicy.NETWORK_BEFORE_STALE)

    @Provides
    fun providesRelatedVideoStore(api: FloatPlaneApi, persistor: RelatedVideoPersistor): StoreRoom<List<Video>, String> = StoreRoom.from({ api.getRelatedVideos(it) }, persistor, StalePolicy.NETWORK_BEFORE_STALE)
}