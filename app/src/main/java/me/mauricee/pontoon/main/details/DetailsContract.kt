package me.mauricee.pontoon.main.details

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video

interface DetailsContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action {
        class Comment(val text: String, val parent: String) : Action()
        class PlayVideo(val id: String) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
        class ViewCreator(val creator: UserRepository.Creator) : Action()
        class SeekTo(val position: Int) : Action()
    }

    sealed class State {
        object Loading : State()
        class VideoInfo(val video: Video) : State()
        class Duration(val duration: Int) : State()
        class PlaybackState(val state: BufferState) : State()
        class Comments(val comments: List<Comment>) : State()
        class Error(type: ErrorType = ErrorType.General) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Progress(val progress: Int, val bufferedProgress: Int) : State()
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

