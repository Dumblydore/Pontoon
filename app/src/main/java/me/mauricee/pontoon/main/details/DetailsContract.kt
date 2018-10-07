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

    interface Navigator {
        fun replyToComment(comment: Comment)
    }

    sealed class Action : EventTracker.Action {
        object ViewCreator : Action()
        class Comment(val text: String) : Action()
        class PlayVideo(val id: String) : Action()
        class SeekTo(val position: Int) : Action()
        class Like(val comment: CommentModel) : Action()
        class Reply(val parent: CommentModel) : Action()
        class Dislike(val comment: CommentModel) : Action()
        class ViewReplies(val comment: CommentModel) : Action()
        class ViewUser(val user: UserRepository.User) : Action()
    }

    sealed class State : EventTracker.State {
        object Loading : State()
        class VideoInfo(val video: Video) : State()
        class Duration(val duration: Int) : State()
        class Like(val comment: CommentModel) : State()
        class Dislike(val comment: CommentModel) : State()
        class PlaybackState(val state: BufferState) : State()
        class Comments(val comments: List<Comment>) : State()
        class CurrentUser(val user: UserRepository.User) : State()
        class RelatedVideos(val relatedVideos: List<Video>) : State()
        class Error(val type: ErrorType = ErrorType.General) : State() {
            override val tag: String
                get() = "${super.tag}_$type"
        }
        class Progress(val progress: Int, val bufferedProgress: Int) : State() {
            override val level: EventTracker.Level
                get() = EventTracker.Level.DEBUG
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

