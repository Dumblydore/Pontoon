package me.mauricee.pontoon.tv.browse

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import me.mauricee.pontoon.repository.video.Video
import me.mauricee.pontoon.tv.util.BaseLeanbackPresenter
import me.mauricee.pontoon.tv.R
import me.mauricee.pontoon.tv.databinding.ItemVideoBinding
import kotlin.properties.Delegates

class VideoPresenterViewHolder : BaseLeanbackPresenter<Video, ItemVideoBinding>() {
    private var mDefaultCardImage: Drawable? = null
    private var sSelectedBackgroundColor: Int by Delegates.notNull()
    private var sDefaultBackgroundColor: Int by Delegates.notNull()


    override fun createViewBinding(parent: ViewGroup): ItemVideoBinding {
        sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background)
        mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.movie)
        return ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
        }
    }

    override fun bind(item: Video, binding: ItemVideoBinding) {
        binding.root.titleText = item.title
        binding.root.contentText = item.creator.name
        binding.root.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        Glide.with(binding.root.context)
                .load(item.thumbnail)
                .centerCrop()
                .error(mDefaultCardImage)
                .into(binding.root.mainImageView)
    }

    override fun unbind(binding: ItemVideoBinding) {
        binding.root.badgeImage = null
        binding.root.mainImage = null
    }

    companion object {
        private val TAG = "CardPresenter"

        private val CARD_WIDTH = 313
        private val CARD_HEIGHT = 176
    }
}