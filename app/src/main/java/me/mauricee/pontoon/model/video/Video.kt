package me.mauricee.pontoon.model.video

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Maybe
import me.mauricee.pontoon.domain.floatplane.Video
import me.mauricee.pontoon.model.user.UserRepository
import org.threeten.bp.Instant

@Entity(tableName = "Video")
data class VideoEntity(@PrimaryKey val id: String,
                       val creator: String,
                       val description: String,
                       val releaseDate: Instant,
                       val duration: Long,
                       val thumbnail: String,
                       val title: String) {
    constructor(video: Video) : this(video.guid, video.creator, video.description, video.releaseDate, video.duration, video.defaultThumbnail, video.title)
}

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg videos: VideoEntity)

    @Query("SELECT * FROM Video WHERE id = :id")
    fun getVideo(id: String): Maybe<VideoEntity>

    @Query("SELECT * FROM Video WHERE creator = :creator")
    fun getVideoByCreator(creator: String): DataSource.Factory<Int, VideoEntity>

    @Query("SELECT * FROM Video WHERE creator = (:creators)")
    fun getVideoByCreators(vararg creators: String): DataSource.Factory<Int, VideoEntity>

}

data class Quality(val p360: String, val p480: String, val p720: String, val p1080: String)
data class Video(val id: String, val title: String, val description: String, val releaseDate: Instant,
                 val duration: Long, val creator: UserRepository.Creator, val thumbnail: String) {
    constructor(video: Video, creator: UserRepository.Creator) : this(video.guid, video.title, video.description, video.releaseDate, video.duration, creator, video.defaultThumbnail)
    constructor(video: VideoEntity, creator: UserRepository.Creator) : this(video.id, video.title, video.description, video.releaseDate, video.duration, creator, video.thumbnail)
}

data class Playback(val video: me.mauricee.pontoon.model.video.Video, val quality: Quality) {}