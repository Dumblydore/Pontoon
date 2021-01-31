package me.mauricee.pontoon.model.user

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import io.reactivex.Observable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.model.DataModel
import me.mauricee.pontoon.ui.main.MainScope
import javax.inject.Inject

@MainScope
class UserRepository @Inject constructor(private val userStore: StoreRoom<User, String>,
                                         private val accountManagerHelper: AccountManagerHelper) {

    val activeUser: Observable<User>
        get() = Observable.fromCallable { User(accountManagerHelper.account.toEntity(), emptyList()) }

    fun getUser(id: String): DataModel<User> = userStore.getAsDataModel(id)
}

