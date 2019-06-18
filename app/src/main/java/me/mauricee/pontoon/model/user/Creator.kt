package me.mauricee.pontoon.model.user

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import io.reactivex.Single
import me.mauricee.pontoon.domain.floatplane.Creator
import me.mauricee.pontoon.domain.floatplane.CreatorListItem
import me.mauricee.pontoon.ext.toObservable
import javax.inject.Inject

@Entity(tableName = "Creator")
class CreatorEntity(@PrimaryKey val id: String, val name: String,
                    val urlName: String, val about: String,
                    val description: String, val owner: String)

@Dao
interface CreatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg creatorEntity: CreatorEntity)

    @Delete
    fun delete(vararg creatorEntity: CreatorEntity)

    @Query("SELECT * FROM Creator WHERE id IN (:creatorIds)")
    fun getCreatorsByIds(vararg creatorIds: String): Observable<List<CreatorEntity>>

    @Query("SELECT * FROM Creator")
    fun getCreators(): Observable<List<CreatorEntity>>

    class Persistor @Inject constructor(private val dao: CreatorDao) : RoomPersister<CreatorEntity, List<CreatorEntity>, String> {
        override fun write(key: String, raw: CreatorEntity) = dao.insert(raw)

        override fun read(key: String): Observable<List<CreatorEntity>> = if (key == All) dao.getCreators() else dao.getCreatorsByIds(key)
    }

    companion object {
        const val All = ""
    }
}

fun Creator.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, owner)

fun CreatorListItem.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, owner.id)