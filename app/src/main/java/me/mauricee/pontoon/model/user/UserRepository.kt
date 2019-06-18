package me.mauricee.pontoon.model.user

import androidx.recyclerview.widget.DiffUtil
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.RxHelpers
import me.mauricee.pontoon.ext.doOnIo
import me.mauricee.pontoon.ext.logd
import me.mauricee.pontoon.ext.loge
import org.threeten.bp.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserRepository @Inject constructor(private val floatPlaneApi: FloatPlaneApi,
                                         private val creatorStoreRoom: StoreRoom<List<CreatorEntity>, String>,
                                         private val userStoreRoom: StoreRoom<List<UserEntity>, String>,
                                         private val accountManagerHelper: AccountManagerHelper) {

    val activeUser by lazy { accountManagerHelper.account.let { User(it.id, it.username, it.profileImage.path) } }

    fun getCreators(vararg creatorIds: String): Observable<List<Creator>> = creatorIds.toObservable().flatMap { creatorStoreRoom.get(it) }
            .flatMapIterable { it }
            .flatMapSingle { creator -> getUsers(creator.owner).map { Creator(creator, it.first()) }.toList() }
            .filter { it.isNotEmpty() }
            .compose(RxHelpers.applyObservableSchedulers())

    fun getAllCreators(): Single<List<Creator>> = floatPlaneApi.allCreators.flatMapIterable { it }
            .flatMap { getUsers(it.owner.id).map { user -> Creator(it.toEntity(), user.first()) } }
            .toList().compose(RxHelpers.applySingleSchedulers())

    fun getUsers(vararg userIds: String): Observable<List<User>> = userIds.toObservable().flatMap { userStoreRoom.get(it) }
            .flatMapSingle { users -> users.toObservable().map { User(it.id, it.username, it.profileImage) }.toList() }
            .compose(RxHelpers.applyObservableSchedulers())

    fun getActivity(user: User): Observable<List<Activity>> = floatPlaneApi.getActivity(user.id).flatMapSingle { response ->
        response.activity.toObservable().map { Activity(it.comment, it.date, it.video.title, it.video.id) }.toList()
    }

    data class Creator(val id: String, val name: String, val url: String, val about: String, val user: User) {

        constructor(creator: CreatorEntity, user: User) : this(creator.id, creator.name, creator.urlName, creator.about, user)

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
            val ItemCallback = object : DiffUtil.ItemCallback<Activity>() {
                override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean = oldItem.comment == newItem.comment

                override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean = newItem == oldItem
            }
        }
    }
}

