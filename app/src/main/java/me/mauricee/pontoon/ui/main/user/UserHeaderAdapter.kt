package me.mauricee.pontoon.ui.main.user

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import me.mauricee.pontoon.R
import me.mauricee.pontoon.common.theme.ThemeManager
import me.mauricee.pontoon.databinding.ItemUserHeaderBinding
import me.mauricee.pontoon.ext.animateStatusBarColor
import me.mauricee.pontoon.ext.updateAsInt
import me.mauricee.pontoon.glide.GlideApp
import me.mauricee.pontoon.model.user.UserEntity
import me.mauricee.pontoon.rx.glide.toPalette
import me.mauricee.pontoon.ui.preferences.darken

internal class UserHeaderAdapter(private val themeManager: ThemeManager,
                                 private val toolbar: Toolbar,
                                 private val fragment: UserFragment) : RecyclerView.Adapter<UserHeaderAdapter.VieWHolder>() {

    var user: UserEntity? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VieWHolder {
        return ItemUserHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                .let(::VieWHolder)
    }

    override fun onBindViewHolder(holder: VieWHolder, position: Int) {
        user?.let {
            holder.binding.userContainerSubtitle.text = fragment.getString(R.string.user_container_subtitle, it.username)
            fragment.subscriptions += Glide.with(fragment).asBitmap().load(it.profileImage).toPalette()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { event ->
                        themeManager.getVibrantSwatch(event.palette)?.let { holder.setUserTheme(event.bitmap, it) }
                        GlideApp.with(holder.itemView).load(event.bitmap).circleCrop().into(holder.binding.userContainerUserIcon)
                    }
        }
    }

    override fun getItemCount(): Int = 1

    inner class VieWHolder(val binding: ItemUserHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setUserTheme(profileBitmap: Bitmap, swatch: Palette.Swatch) {
            fragment.animations += AnimatorSet().apply {
                playTogether(fragment.requireActivity().animateStatusBarColor(swatch.rgb.darken(.7f)),
                        ValueAnimator.ofArgb(swatch.rgb).updateAsInt(toolbar::setBackgroundColor),
                        ValueAnimator.ofArgb(swatch.rgb.darken(.5f)).updateAsInt(binding.userContainerHeader::setBackgroundColor),
                        ValueAnimator.ofArgb(swatch.titleTextColor).updateAsInt {
                            toolbar.setTitleTextColor(it)
                            toolbar.navigationIcon?.mutate()?.setTint(it)
                        })
                start()
            }
        }
    }
}