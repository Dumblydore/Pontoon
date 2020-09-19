package me.mauricee.pontoon.main.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.item_activity_comment.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.BaseListAdapter
import me.mauricee.pontoon.common.SimpleListAdapter
import me.mauricee.pontoon.model.user.UserRepository
import me.mauricee.pontoon.model.user.activity.ActivityEntity
import javax.inject.Inject

class UserActivityAdapter @Inject constructor() : BaseListAdapter<UserContract.Action, ActivityEntity, UserActivityAdapter.ViewHolder>(SimpleListAdapter.ItemCallback()) {

    override fun getItemViewType(position: Int): Int = R.layout.item_activity_comment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false).let(this::ViewHolder)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            subscriptions += view.clicks().subscribe {
                relay.accept(getItem(adapterPosition).postId.let(UserContract.Action::Video))
            }
        }

        //TODO
        fun bind(activity: ActivityEntity) {
            itemView.item_title.text = itemView.context.getString(R.string.activity_comment_context, "TEST")
            itemView.item_comment.text = activity.comment
        }
    }
}