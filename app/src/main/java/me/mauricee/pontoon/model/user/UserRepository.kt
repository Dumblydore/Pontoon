package me.mauricee.pontoon.model.user

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.doOnIo
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepository @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                         private val userDao: UserDao,
                                         private val creatorDao: CreatorDao,
                                         private val accountManagerHelper: AccountManagerHelper) {

    val activeUser by lazy { accountManagerHelper.account.let { UserRepository.User(it.id, it.username, it.profileImage.path) } }

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

    fun getUsers(vararg userIds: String): Observable<List<User>> =
            getUsersFromNetwork(*userIds).filter { it.isNotEmpty() }
                    .map { it.map { User(it.id, it.username, it.profileImage) } }
                    .toObservable().compose(RxHelpers.applyObservableSchedulers())

    fun getActivity(user: User): Observable<List<Activity>> = floatPlaneApi.getActivity(user.id)
            .flatMapSingle { response ->
                response.activity.toObservable().map { Activity(it.comment, it.date, it.video.title, it.video.id) }.toList()
            }

    private fun getCreatorsFromNetwork(vararg creatorIds: String) =
            floatPlaneApi.getCreator(*creatorIds).flatMap { it.toObservable() }
                    .map { CreatorEntity(it.id, it.title, it.urlname, it.about, it.description, it.owner) }
                    .toList().toObservable()


    private fun getUsersFromNetwork(vararg userIds: String): Single<List<UserEntity>> =
            userIds.toObservable().buffer(20).flatMap { floatPlaneApi.getUsers(*it.toTypedArray()) }
                    .flatMap { it.users.toObservable().map { it.user } }
                    .map { UserEntity(it.id, it.username, it.profileImage.path) }
                    .toList().doOnSuccess(this::cacheUsers)

    private fun cacheUsers(it: MutableList<UserEntity>) {
        Completable.fromCallable { userDao.insert(*it.toTypedArray()) }.doOnIo()
                .onErrorComplete().subscribe()
    }

    data class Creator(val id: String, val name: String, val url: String,
                       val about: String, val user: User) {
        companion object {
            val ItemCallback = object : DiffUtil.ItemCallback<Creator>() {
                override fun areItemsTheSame(oldItem: Creator, newItem: Creator): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Creator, newItem: Creator): Boolean = newItem == oldItem
            }
        }
    }

    data class User(val id: String, val username: String, val profileImage: String)

    data class Activity(val comment: String, val posted: Instant, val videoTitle: String, val videoId: String) {
        companion object {
            val ItemCallback = object:  DiffUtil.ItemCallback<Activity>() {
                override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean = oldItem.comment == newItem.comment

                override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean = newItem == oldItem
            }
        }
    }
}

