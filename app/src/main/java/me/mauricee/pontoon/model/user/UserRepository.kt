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
                                         private val accountManagerHelper: AccountManagerHelper) {

    //TODO make this reactive?
    val activeUser by lazy { accountManagerHelper.account.let { UserRepository.User(it.id, it.username, it.profileImage.path) } }

    fun getUser(id: String): Observable<me.mauricee.pontoon.model.user.User> = userStore.getAndFetch(id)

    fun getCreators(vararg creatorIds: String): Observable<List<Creator>> = Observable.empty()

    @Deprecated("", ReplaceWith("UserRepository.getUser()", "me.mauricee.pontoon.model.user"))
    fun getUsers(vararg userIds: String): Observable<List<User>> = Observable.empty()

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

