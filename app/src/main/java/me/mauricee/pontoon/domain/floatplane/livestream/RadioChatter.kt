package me.mauricee.pontoon.domain.floatplane.livestream

//42 if it's radiochatter, 434 if it's a response from Chat
data class RadioChatter(
        val id: String,
        val userId: String,
        val username: String,
        val channel: String,
        val message: String,
        val type: UserType,
        val success: Boolean
)

enum class UserType {
    Normal,
    Moderator
}