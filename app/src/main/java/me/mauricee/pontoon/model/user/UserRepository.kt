package me.mauricee.pontoon.model.user

import androidx.recyclerview.widget.DiffUtil
import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.main.MainScope
import org.threeten.bp.Instant
import javax.inject.Inject

@MainScope
class UserRepository @Inject constructor(private val userStore: StoreRoom<me.mauricee.pontoon.model.user.User, String>,
                                         private val accountManagerHelper: AccountManagerHelper) {

    //TODO make this reactive?
    val activeUser by lazy { accountManagerHelper.account.let { UserRepository.User(it.id, it.username, it.profileImage.path) } }

    fun getUser(id: String): Observable<me.mauricee.pontoon.model.user.User> = userStore.getAndFetch(id)

    @Deprecated("", ReplaceWith("UserRepository.getUser()", "me.mauricee.pontoon.model.user"))
    fun getUsers(vararg userIds: String): Observable<List<User>> = Observable.empty()

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

