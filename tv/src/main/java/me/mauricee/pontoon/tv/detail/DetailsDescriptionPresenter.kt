package me.mauricee.pontoon.tv.detail

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.tv.Movie

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {

    override fun onBindDescription(
            viewHolder: AbstractDetailsDescriptionPresenter.ViewHolder,
            item: Any) {
        val movie = item as Video

        viewHolder.title.text = movie.title
        viewHolder.subtitle.text = movie.creator.name
        viewHolder.body.text = movie.description
    }
}