package me.mauricee.pontoon.model.user

import com.nytimes.android.external.store3.base.impl.room.StoreRoom
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.Observable
import me.mauricee.pontoon.domain.floatplane.FloatPlaneApi
import me.mauricee.pontoon.ext.getAsDataModel
import me.mauricee.pontoon.model.DataModel
import javax.inject.Inject

@ActivityRetainedScoped
class UserRepository @Inject constructor(private val userStore: StoreRoom<User, String>,
                                         private val flowaPlaneApi: FloatPlaneApi) {

    val activeUser: Observable<User>
        get() = flowaPlaneApi.self.map { User(it.toEntity(), emptyList()) }.toObservable()

    fun getUser(id: String): DataModel<User> = userStore.getAsDataModel(id)
}

