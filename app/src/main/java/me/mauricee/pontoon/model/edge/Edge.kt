package me.mauricee.pontoon.model.edge

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@Entity(tableName = "Edges")
class EdgeEntity(val allowStreaming: Boolean, val allowDownloads: Boolean, val hostname: String) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
abstract class EdgeDao {

    @Query("SELECT hostname from Edges Where allowStreaming = 1")
    abstract fun getStreamingEdgeHosts(): Observable<List<String>>

    @Query("SELECT hostname from Edges Where allowDownloads = 1")
    abstract fun getDownloadEdgeHosts(): Observable<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addEdges(edges: List<EdgeEntity>)

    @Query("DELETE FROM Edges")
    abstract fun clear()

    @Query("SELECT COUNT(id) FROM Edges")
    abstract fun size(): Int

    @Transaction
    open fun updateEdges(edges: List<EdgeEntity>) {
        clear()
        addEdges(edges)
    }

    class Persistor @Inject constructor(private val dao: EdgeDao) : RoomPersister<List<EdgeEntity>, List<String>, Persistor.EdgeType> {
        override fun write(key: EdgeType, raw: List<EdgeEntity>) = dao.updateEdges(raw)

        override fun read(key: EdgeType): Observable<List<String>> = when (key) {
            EdgeType.Streaming -> dao.getStreamingEdgeHosts()
            EdgeType.Download -> dao.getDownloadEdgeHosts()
        }

        enum class EdgeType {
            Streaming,
            Download
        }
    }
}