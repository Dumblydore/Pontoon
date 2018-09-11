package me.mauricee.pontoon.model.edge

import androidx.room.*
import io.reactivex.Single

@Entity(tableName = "Edges")
class EdgeEntity(val allowStreaming: Boolean, val allowDownloads: Boolean, val hostname: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
abstract class EdgeDao {

    @Query("SELECT hostname from Edges Where allowStreaming = 1 ORDER BY RANDOM() LIMIT 1")
    abstract fun getStreamingEdgeHost(): Single<String>

    @Query("SELECT hostname from Edges Where allowDownloads = 1 ORDER BY RANDOM() LIMIT 1")
    abstract fun getDownloadEdgeHost(): Single<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addEdges(edges: List<EdgeEntity>)

    @Query("DELETE FROM Edges")
    abstract fun clear()

    @Query("SELECT COUNT(id) FROM Edges")
    abstract fun size() : Int

    @Transaction
    open fun updateEdges(edges: List<EdgeEntity>) {
        clear()
        addEdges(edges)
    }
}