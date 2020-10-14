package me.mauricee.pontoon.ui.main.videos

import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_header_video_card.view.*
import me.mauricee.pontoon.R
import me.mauricee.pontoon.ui.main.VideoPageAdapter
import javax.inject.Inject

class SubscriptionVideoAdapter @Inject constructor(val subscriptionAdapter: SubscriptionAdapter) : VideoPageAdapter() {

    override fun getItemViewType(position: Int): Int = if (position == 0)
        R.layout.item_header_video_card else super.getItemViewType(position)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0) {
            holder.itemView.item_subscriptions.apply {
                adapter = subscriptionAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
        }
    }
}