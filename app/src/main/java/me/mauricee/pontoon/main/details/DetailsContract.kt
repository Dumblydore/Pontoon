package me.mauricee.pontoon.main.details

import me.mauricee.pontoon.BaseContract
import me.mauricee.pontoon.analytics.EventTracker
import me.mauricee.pontoon.model.comment.Comment
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.video.Video


private typealias CommentModel = Comment

interface DetailsContract {
    interface View : BaseContract.View<State, Action>

    interface Presenter : BaseContract.Presenter<View>

    sealed class Action : EventTracker.Action {
        class Comment(val text: String, val parent: String) : Action()
        class PlayVideo(val id: String) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
        class ViewCreator(val creator: UserRepository.Creator) : Action()
        class SeekTo(val position: Int) : Action()
        class Like(val comment: CommentModel) : Action()
        class Dislike(val comment: CommentModel) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class VideoInfo(val video: Video) : State()
        class Duration(val duration: Int) : State()
        class PlaybackState(val state: BufferState) : State()
        class Comments(val comments: List<Comment>) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Progress(val progress: Int, val bufferedProgress: Int) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
        }
        class Like(val comment: CommentModel) : State()
        class Dislike(val comment: CommentModel) : State()
        class Error(val type: ErrorType = ErrorType.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
        }
    }

    enum class ErrorType {
        NoComments,
        NoVideo,
        NoRelatedVideos,
        General,
        Post,
        Like,
        Dislike
    }

    enum class BufferState {
        Playing,
        Buffering
    }

}

