package me.mauricee.pontoon.model.creator

import androidx.room.*
import com.nytimes.android.external.store3.base.room.RoomPersister
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.CreatorJson
import me.mauricee.pontoon.domain.floatplane.CreatorListItem
import me.mauricee.pontoon.domain.floatplane.UserJson
import me.mauricee.pontoon.model.BaseDao
import me.mauricee.pontoon.model.Diffable
import me.mauricee.pontoon.model.user.UserDao
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.model.user.toEntity
import javax.inject.Inject

@Entity(tableName = "Creator", foreignKeys = [ForeignKey(parentColumns = ["id"], childColumns = ["owner"], entity = UserEntity::class, onDelete = ForeignKey.CASCADE)])
class CreatorEntity(@PrimaryKey val id: String,
                    val name: String,
                    val urlName: String,
                    val about: String,
                    val description: String,
                    val owner: String)

data class Creator(@Embedded val entity: CreatorEntity,
                   @Relation(parentColumn = "owner", entityColumn = "id") val user: UserEntity) : Diffable<String> {
    @Ignore
    override val id: String = entity.id
}

fun CreatorJson.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, owner)
fun CreatorListItem.toEntity(): CreatorEntity = CreatorEntity(id, title, urlname, about, description, owner.id)

@Dao
abstract class CreatorDao : BaseDao<CreatorEntity>() {
    @Query("SELECT * FROM Creator")
    abstract fun getCreators(): Observable<List<Creator>>

    @Query("SELECT * FROM Creator WHERE id=:creatorId")
    abstract fun getCreator(creatorId: String): Observable<Creator>
}

class CreatorPersistor @Inject constructor(private val creatorDao: CreatorDao,
                                           private val userDao: UserDao) : RoomPersister<CreatorPersistor.Raw, Creator, String> {
    override fun read(key: String): Observable<Creator> = creatorDao.getCreator(key)

    override fun write(key: String, raw: Raw) {
        userDao.upsert(raw.owner.toEntity())
        creatorDao.upsert(raw.creator.toEntity())
    }

    data class Raw(val creator: CreatorJson, val owner: UserJson)
}

class AllCreatorPersistor @Inject constructor(private val creatorDao: CreatorDao,
                                              private val userDao: UserDao) : RoomPersister<List<AllCreatorPersistor.Raw>, List<Creator>, Unit> {

    override fun read(key: Unit): Observable<List<Creator>> = creatorDao.getCreators()

    override fun write(key: Unit, raw: List<Raw>) {
        val (creators, users) = raw.map { it.creator.toEntity() to it.owner.toEntity() }
                .unzip()
        userDao.upsert(users)
        creatorDao.upsert(creators)
    }

    data class Raw(val creator: CreatorListItem, val owner: UserJson)
}