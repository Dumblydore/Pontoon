package me.mauricee.pontoon.model.user

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.Observable
import me.mauricee.pontoon.domain.account.AccountManagerHelper
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.model.DataModel
import javax.inject.Inject

@ActivityRetainedScoped
class UserRepository @Inject constructor(private val userStore: StoreRoom<User, String>,
                                         private val accountManagerHelper: AccountManagerHelper) {

    val activeUser: Observable<User>
        get() = Observable.fromCallable { User(accountManagerHelper.account.toEntity(), emptyList()) }

    fun getUser(id: String): DataModel<User> = userStore.getAsDataModel(id)
}

