package me.mauricee.pontoon.main.details

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface DetailsContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class Comment(val text: String, val parent: String) : Action()
        class PlayVideo(val id: String) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
        class ViewCreator(val creator: UserRepository.Creator) : Action()
        class SeekTo(val position: Int) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class VideoInfo(val video: Video) : State()
        class Comments(val comments: List<Comment>) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Error(val type: ErrorType = ErrorType.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
        }
    }

    enum class ErrorType {
        NoComments,
        NoVideo,
        NoRelatedVideos,
        General
    }

    enum class BufferState {
        Playing,
        Buffering
    }

}

