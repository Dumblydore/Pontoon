package me.mauricee.pontoon.model.livestream

import me.mauricee.pontoon.domain.floatplane.livestream.UserType

data class ChatItem(val id: String, val message: String, val type: UserType)