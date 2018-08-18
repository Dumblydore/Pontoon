package me.mauricee.pontoon.model.user

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.common.CacheValidator
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import org.threeten.bp.Instant
import javax.inject.Inject

class UserRepository @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                         private val userDao: UserDao,
                                         private val creatorDao: CreatorDao,
                                         cacheValidatorB: CacheValidator.Factory) {

    private val userCacheValidator = cacheValidatorB.newInstance("Users")

    fun getCreators(vararg creatorIds: String): Observable<List<Creator>> = userCacheValidator
            .check({ creatorDao.getCreatorsByIds(*creatorIds) }, { getCreatorsFromNetwork(*creatorIds) })
            .let {
                Single.concat(creatorDao.getCreatorsByIds(*creatorIds), it).toObservable()
                        .filter { it.isNotEmpty() }
                        .flatMapSingle(this::loadCreators)
            }.compose(RxHelpers.applyObservableSchedulers())

    private fun loadCreators(creators: List<CreatorEntity>) = creators.map { it.owner }.let { ids ->
        getUsers(*ids.toTypedArray()).map { users -> Pair(creators, users) }.flatMap { pair ->
            pair.first.toObservable().map { creator ->
                val owner = pair.second.first { creator.owner == it.id }
                Creator(creator.id, creator.name, creator.urlName, creator.coverImage, creator.about, owner)
            }
        }.toList()
    }

    fun getUsers(vararg userIds: String): Observable<List<User>> =
            Single.concat(userDao.getUsersByIds(*userIds), getUsersFromNetwork(*userIds))
                    .filter { it.isNotEmpty() }
                    .map { it.map { User(it.id, it.username, it.profileImage) } }
                    .distinctUntilChanged().toObservable()
                    .compose(RxHelpers.applyObservableSchedulers())

    fun getActivity(user: User): Observable<List<Activity>> = floatPlaneApi.getActivity(user.id)
            .flatMapSingle {
                it.activity.toObservable().map { Activity(it.comment, it.date, it.video.id) }.toList()
            }

    fun getCreatorsFromNetwork(vararg creatorIds: String) =
            floatPlaneApi.getCreator(*creatorIds).flatMap { it.toObservable() }
                    .map { CreatorEntity(it.id, it.title, it.urlname, it.cover.path, it.about, it.description, it.owner) }
                    .toList()

    private fun getUsersFromNetwork(vararg userIds: String): Single<List<UserEntity>> =
            userIds.toObservable().buffer(20).flatMap { floatPlaneApi.getUsers(*it.toTypedArray()) }
                    .flatMap { it.users.toObservable().map { it.user } }
                    .map { UserEntity(it.id, it.username, it.profileImage.path) }
                    .toList().doOnSuccess { userDao.insert(*it.toTypedArray()) }

    data class Creator(val id: String, val name: String, val url: String,
                       val coverImage: String, val about: String, val user: User)

    data class User(val id: String, val username: String, val profileImage: String)

    data class Activity(val comment: String, val posted: Instant, val video: String)
}

