package me.mauricee.pontoon.data.local.video

import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.data.local.BaseDao
import me.mauricee.pontoon.data.local.creator.Creator
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import org.threeten.bp.Instant

@Entity(tableName = "Videos", indices = [Index("creator")], foreignKeys = [ForeignKey(entity = CreatorEntity::class, parentColumns = ["id"], childColumns = ["creator"], onDelete = ForeignKey.CASCADE)])
data class VideoEntity(@PrimaryKey val id: String,
                       val creator: String,
                       val description: String,
                       val releaseDate: Instant,
                       val duration: Long,
                       val thumbnail: String,
                       val title: String,
                       val watched: Instant?)

@Entity(tableName = "RelatedVideos", primaryKeys = ["originalVideoId", "relatedVideoId"],
        indices = [Index("originalVideoId", "relatedVideoId", unique = true)],
        foreignKeys = [ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["originalVideoId"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["relatedVideoId"], onDelete = ForeignKey.CASCADE)])
data class RelatedVideo(val originalVideoId: String, val relatedVideoId: String)

data class Video(@Embedded
                 val entity: VideoEntity,
                 @Relation(parentColumn = "creator", entityColumn = "id", entity = CreatorEntity::class)
                 val creator: Creator) : Diffable<String> {

    @Ignore
    override val id: String = entity.id

    fun toBrowsableUrl(): String = "https://www.floatplane.com/video/$id"
}

@Dao
abstract class VideoDao : BaseDao<VideoEntity>() {
    @Query("SELECT COUNT(id) FROM Videos")
    abstract fun getNumberOfVideos(): Int

    @Query("SELECT COUNT(id) FROM Videos WHERE creator = :creator")
    abstract fun getNumberOfVideosByCreator(creator: String): Int

    @Query("SELECT * FROM Videos WHERE id = :id")
    abstract fun getVideo(id: String): Observable<Video>

    @Query("SELECT * FROM Videos WHERE LOWER(title) LIKE LOWER(:query) AND creator in (:creators)")
    abstract fun search(query: String, vararg creators: String): DataSource.Factory<Int, Video>

    @Query("SELECT * FROM Videos WHERE creator IN (:creators) ORDER BY releaseDate DESC")
    abstract fun getVideoByCreators(vararg creators: String): DataSource.Factory<Int, Video>

    @Query("SELECT * FROM Videos WHERE creator IN (:creators) AND watched IS NULL ORDER BY releaseDate DESC")
    abstract fun getUnwatchedVideosByCreators(vararg creators: String): DataSource.Factory<Int, Video>

    @Query("SELECT * FROM Videos WHERE watched IS NOT NULL ORDER BY watched DESC")
    abstract fun history(): DataSource.Factory<Int, Video>

    @Query("UPDATE Videos SET watched = :watched WHERE id = :id")
    abstract fun setWatched(id: String, watched: Instant = Instant.now()): Completable

    @Query("DELETE From Videos WHERE creator IN (:creators)")
    abstract fun clearCreatorVideos(vararg creators: String) : Completable

    @Query("SELECT * FROM Videos INNER JOIN RelatedVideos ON Videos.id=RelatedVideos.relatedVideoId WHERE RelatedVideos.originalVideoId=:videoId LIMIT 5")
    abstract fun getRelatedVideos(videoId: String): Observable<List<Video>>
}

@Dao
abstract class RelatedVideoDao : BaseDao<RelatedVideo>() {

    @Query("DELETE FROM RelatedVideos Where originalVideoId=:videoId")
    abstract fun delete(videoId: String)

    @Transaction
    open fun deleteInsert(videoId: String, relatedVideos: List<RelatedVideo>) {
        delete(videoId)
        insert(relatedVideos)
    }
}
