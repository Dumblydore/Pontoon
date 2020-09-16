package me.mauricee.pontoon.model.user

import androidx.recyclerview.widget.DiffUtil
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.main.MainScope
import me.mauricee.pontoon.model.creator.CreatorDao
import me.mauricee.pontoon.model.creator.CreatorEntity
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@MainScope
class UserRepository @Inject constructor(private val userStore: StoreRoom<me.mauricee.pontoon.model.user.User, String>,
                                         private val floatPlaneApi: FloatPlaneApi,
                                         private val userDao: UserDao,
                                         private val creatorDao: CreatorDao,
                                         private val accountManagerHelper: AccountManagerHelper) {

    //TODO make this reactive?
    val activeUser by lazy { accountManagerHelper.account.let { UserRepository.User(it.id, it.username, it.profileImage.path) } }

    fun getUser(id: String): Observable<me.mauricee.pontoon.model.user.User> = userStore.getAndFetch(id)

    fun getCreators(vararg creatorIds: String): Observable<List<Creator>> =
            Observable.mergeArray(creatorDao.getCreatorsByIds(*creatorIds), getCreatorsFromNetwork(*creatorIds))
                    .flatMapSingle(this::loadCreators)
                    .filter { it.isNotEmpty() }
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .compose(RxHelpers.applyObservableSchedulers())

    fun getAllCreators(): Observable<List<Creator>> = floatPlaneApi.allCreators.flatMapIterable { it }
            .filter { it.subscriptions.isNotEmpty() }
            .map { CreatorEntity(it.id, it.title, it.urlname, it.about, it.description, it.owner.id) }
            .toList().flatMap {
                creatorDao.insert(*it.toTypedArray())
                loadCreators(it)
            }.toObservable().compose(RxHelpers.applyObservableSchedulers())

    private fun loadCreators(creators: List<CreatorEntity>) = creators.map { it.owner }.let { ids ->
        getUsers(*ids.toTypedArray()).map { users -> Pair(creators, users) }.flatMap { pair ->
            pair.first.toObservable().map { creator ->
                val owner = pair.second.first { creator.owner == it.id }
                Creator(creator.id, creator.name, creator.urlName, creator.about, owner)
            }
        }.toList()
    }

    @Deprecated("", ReplaceWith("UserRepository.getUser()", "me.mauricee.pontoon.model.user"))
    fun getUsers(vararg userIds: String): Observable<List<User>> = Observable.empty()

    private fun getCreatorsFromNetwork(vararg creatorIds: String) =
            floatPlaneApi.getCreator(*creatorIds).flatMap { it.toObservable() }
                    .map { CreatorEntity(it.id, it.title, it.urlname, it.about, it.description, it.owner) }
                    .toList().toObservable()

    data class Creator(val id: String, val name: String, val url: String,
                       val about: String, val user: User) {
        companion object {
            val ItemCallback = object : DiffUtil.ItemCallback<Creator>() {
                override fun areItemsTheSame(oldItem: Creator, newItem: Creator): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Creator, newItem: Creator): Boolean = newItem == oldItem
            }
        }
    }

    @Deprecated("", ReplaceWith("User", "me.mauricee.pontoon.model.user"))
    data class User(val id: String, val username: String, val profileImage: String)

    data class Activity(val comment: String, val posted: Instant, val videoTitle: String, val videoId: String) {
        companion object {
            val ItemCallback = object : DiffUtil.ItemCallback<Activity>() {
                override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean = oldItem.comment == newItem.comment

                override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean = newItem == oldItem
            }
        }
    }
}

