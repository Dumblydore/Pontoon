package me.mauricee.pontoon.domain.github

import com.google.gson.annotations.SerializedName

data class Release(@SerializedName("tag_name") val tagName: String) {
    val versionCode: Int by lazy {
        val (major, minor, patch) = tagName.split(".")
                .map { it.toInt() }.let { if (it.size > 3) it.subList(0, 3) else it }
        (major * 100) + (minor * 100) + patch
    }
}