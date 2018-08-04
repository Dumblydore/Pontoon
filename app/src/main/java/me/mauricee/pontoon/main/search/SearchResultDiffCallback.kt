package me.mauricee.pontoon.main.search

import androidx.recyclerview.widget.DiffUtil
import me.mauricee.pontoon.model.video.Video
import javax.inject.Inject

class SearchResultDiffCallback @Inject constructor() : DiffUtil.Callback() {

    private var currentList: List<Video> = emptyList()
    private var oldList: List<Video> = emptyList()

    fun submit(newList: List<Video>): SearchContract.State {
        currentList = newList
        return SearchContract.State.Results(DiffUtil.calculateDiff(this), currentList)
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition].id == currentList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = currentList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == currentList[newItemPosition]
}