package me.mauricee.pontoon.repository.user

import com.dropbox.android.external.store4.Store
import dagger.hilt.android.scopes.ActivityRetainedScoped
import me.mauricee.pontoon.repository.DataModel
import me.mauricee.pontoon.repository.util.store.getAsDataModel
import javax.inject.Inject

@ActivityRetainedScoped
class UserRepository @Inject constructor(private val userStore: Store<String, User>) {
    fun getUser(id: String): DataModel<User> = userStore.getAsDataModel(id)
}

