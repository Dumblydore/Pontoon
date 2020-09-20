package me.mauricee.pontoon.model.user

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.getAndFetch
import me.mauricee.pontoon.main.MainScope
import javax.inject.Inject

@MainScope
class UserRepository @Inject constructor(private val userStore: StoreRoom<User, String>,
                                         private val accountManagerHelper: AccountManagerHelper) {

    val activeUser: Observable<User>
        get() = userStore.get(accountManagerHelper.account.id).firstElement().toObservable()

    fun getUser(id: String): Observable<User> = userStore.getAndFetch(id)
}

