package me.mauricee.pontoon.repository.video

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.store.rx2.ofFlowable
import com.dropbox.store.rx2.ofSingle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import me.mauricee.pontoon.data.local.PontoonDatabase
import me.mauricee.pontoon.data.local.video.RelatedVideoDao
import me.mauricee.pontoon.data.local.video.VideoCreatorJoin
import me.mauricee.pontoon.data.local.video.VideoDao
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.video.VideoJson

@Module
@InstallIn(ActivityRetainedComponent::class)
class VideoModelModule {

    @Provides
    fun PontoonDatabase.providesVideoDao(): VideoDao = videoDao

    @Provides
    fun PontoonDatabase.providesRelatedVideoDao(): RelatedVideoDao = relatedVideoDao

    @Provides
    fun providesVideoStore(api: FloatPlaneApi, videoDao: VideoDao): Store<String, Video> {
        return StoreBuilder.from(Fetcher.ofSingle { api.getVideo(it).map(VideoJson::toEntity) },
                SourceOfTruth.ofFlowable(
                        reader = { videoDao.getVideo(it).map(VideoCreatorJoin::toModel) },
                        writer = { _, it -> videoDao.upsertAsync(it) },
                        delete = videoDao::removeVideo,
                        deleteAll = videoDao::removeVideos)).build()
    }

    @Provides
    fun providesRelatedVideoStore(api: FloatPlaneApi, videoDao: VideoDao): Store<String, List<Video>> {
        return StoreBuilder.from(Fetcher.ofSingle { api.getRelatedVideos(it).map { it.map(VideoJson::toEntity) } },
                SourceOfTruth.ofFlowable(
                        reader = { videoDao.getRelatedVideos(it).map { it.map(VideoCreatorJoin::toModel) } },
                        writer = { _, it -> videoDao.upsertAsync(it) },
                        delete = videoDao::removeVideo,
                        deleteAll = videoDao::removeVideos)).build()
    }
}