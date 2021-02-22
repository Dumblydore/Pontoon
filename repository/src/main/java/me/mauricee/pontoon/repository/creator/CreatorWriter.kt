package me.mauricee.pontoon.repository.creator

import io.reactivex.Completable
import io.reactivex.Observable
import me.mauricee.pontoon.data.local.creator.CreatorDao
import me.mauricee.pontoon.data.local.user.UserDao
import me.mauricee.pontoon.data.network.creator.info.CreatorJson
import javax.inject.Inject

class CreatorWriter @Inject constructor(private val creatorDao: CreatorDao,
                                        private val userDao: UserDao) {
    fun write(creator: CreatorJson): Completable = Completable.fromAction {
//        userDao.upsert(creator.owner.toEntity())
        creatorDao.upsert(creator.toEntity())
    }

    fun writeList(creators: List<CreatorJson>): Completable = Observable.fromIterable(creators)
            .flatMapCompletable(::write)
}