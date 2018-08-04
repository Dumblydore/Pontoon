package me.mauricee.pontoon.model.video

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single
import me.mauricee.pontoon.domain.floatplane.Video
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

    @Query("SELECT COUNT(id) FROM Video")
    fun getNumberOfRows(): Int

    @Query("SELECT * FROM Video WHERE id = :id")
    fun getVideo(id: String): Maybe<VideoEntity>

    @Query("SELECT * FROM Video WHERE creator = :creator AND title LIKE :query")
    fun search(query: String, creator: String): Single<List<VideoEntity>>

    @Query("SELECT * FROM Video WHERE creator = :creator")
    fun getVideoByCreator(creator: String): DataSource.Factory<Int, VideoEntity>

    @Query("SELECT * FROM Video WHERE creator = (:creators)")
    fun getVideoByCreators(vararg creators: String): DataSource.Factory<Int, VideoEntity>

    @Query("SELECT * FROM Video, History ORDER BY history.watched DESC")
    fun history(): DataSource.Factory<Int, VideoHistory>

}
