package me.mauricee.pontoon.repository.subscription

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.FetcherResult
import com.dropbox.android.external.store4.SourceOfTruth
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.local.creator.CreatorEntity
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.data.local.subscription.SubscriptionDao
import me.mauricee.pontoon.data.local.subscription.SubscriptionEntity
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.local.user.UserEntity
import me.mauricee.pontoon.data.network.FloatPlaneApi
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import me.mauricee.pontoon.data.network.user.UserJson
import me.mauricee.pontoon.data.network.user.subscription.SubscriptionJson
import me.mauricee.pontoon.repository.creator.Creator
import me.mauricee.pontoon.repository.creator.toEntity
import me.mauricee.pontoon.repository.creator.toModel
import me.mauricee.pontoon.repository.user.toEntity
import javax.inject.Inject


fun SubscriptionJson.toEntity(): SubscriptionEntity = SubscriptionEntity(creatorId, plan.id, startDate, endDate)

class SubscriptionFetcher @Inject constructor(private val api: FloatPlaneApi) : Fetcher<Unit, List<SubscriptionSourceOfTruth.Raw>> {
    override fun invoke(key: Unit): Flow<FetcherResult<List<SubscriptionSourceOfTruth.Raw>>> {
        return api.subscriptions.flatMap { subscriptions ->
            val creatorIds = subscriptions.map { it.creatorId }.distinct().toTypedArray()
            api.getCreators(*creatorIds).flatMap { creators ->
                val creatorMap = creators.map { it.id to it }.toMap()
                val userIds = creators.map { it.owner }.distinct().toTypedArray()
                api.getUsers(*userIds).map { users ->
                    val userMap = users.users.map { it.id to it }.toMap()
                    subscriptions.map {
                        val creator = creatorMap[it.creatorId] ?: error("")
                        val user = userMap[creator.owner]?.user ?: error("")
                        SubscriptionSourceOfTruth.Raw(it, creator, user)
                    }
                }
            }
        }.map<FetcherResult<List<SubscriptionSourceOfTruth.Raw>>> { FetcherResult.Data(it) }
                .onErrorReturn { FetcherResult.Error.Exception(it) }
                .toFlowable().asFlow()
    }

}

class SubscriptionSourceOfTruth @Inject constructor(private val subscriptionDao: SubscriptionDao,
                                                    private val creatorDao: CreatorDao,
                                                    private val userDao: UserDao) : SourceOfTruth<Unit, List<SubscriptionSourceOfTruth.Raw>, List<Creator>> {

    data class Raw(val subscription: SubscriptionJson, val creator: CreatorJson, val user: UserJson)

    override suspend fun delete(key: Unit) = deleteAll()

    override suspend fun deleteAll() = subscriptionDao.removeSubscriptions().await()

    override fun reader(key: Unit): Flow<List<Creator>> {
        return subscriptionDao.getSubscriptions().map { it.map(CreatorUserJoin::toModel) }.asFlow()
    }

    override suspend fun write(key: Unit, value: List<Raw>) = Completable.fromAction {
        val users = mutableListOf<UserEntity>()
        val creators = mutableListOf<CreatorEntity>()
        val subscriptions = mutableListOf<SubscriptionEntity>()
        value.forEach {
            users.add(it.user.toEntity())
            creators.add(it.creator.toEntity())
            subscriptions.add(it.subscription.toEntity())
        }
        userDao.upsert(users)
        creatorDao.upsert(creators)
        subscriptionDao.upsert(subscriptions)
    }.await()
}