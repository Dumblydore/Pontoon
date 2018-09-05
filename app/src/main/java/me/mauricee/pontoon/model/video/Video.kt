package me.mauricee.pontoon.model.video

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Maybe
import org.threeten.bp.Instant

@Entity(tableName = "Video")
data class VideoEntity(@PrimaryKey val id: String,
                       val creator: String,
                       val description: String,
                       val releaseDate: Instant,
                       val duration: Long,
                       val thumbnail: String,
                       val title: String)

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg videos: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun cacheVideos(vararg videos: VideoEntity) : List<Long>

    @Query("SELECT COUNT(id) FROM Video")
    fun getNumberOfVideos(): Int

    @Query("SELECT COUNT(id) FROM Video WHERE creator = :creator")
    fun getNumberOfVideosByCreator(creator: String): Int

    @Query("SELECT * FROM Video WHERE id = :id")
    fun getVideo(id: String): Maybe<VideoEntity>

    @Query("SELECT * FROM Video WHERE title LIKE :query AND creator in (:creators)" )
    fun search(query: String, vararg creators: String): DataSource.Factory<Int, VideoEntity>

    @Query("SELECT * FROM Video WHERE creator IN (:creators) ORDER BY releaseDate DESC")
    fun getVideoByCreators(vararg creators: String): DataSource.Factory<Int, VideoEntity>

    @Query("SELECT * FROM Video, History ORDER BY history.watched DESC")
    fun history(): DataSource.Factory<Int, VideoHistory>

}
