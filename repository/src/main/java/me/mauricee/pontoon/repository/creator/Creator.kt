package me.mauricee.pontoon.repository.creator

import mauricee.me.pontoon.data.common.Diffable
import me.mauricee.pontoon.data.local.creator.CreatorUserJoin
import me.mauricee.pontoon.repository.user.User
import me.mauricee.pontoon.repository.user.toModel

data class Creator(override val id: String,
                   val name: String,
                   val urlName: String,
                   val about: String,
                   val description: String,
                   val owner: User) : Diffable<String>

fun CreatorUserJoin.toModel(): Creator {
    return Creator(entity.id, entity.name, entity.urlName, entity.about, entity.description, user.toModel())
}