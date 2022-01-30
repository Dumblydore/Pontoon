package me.mauricee.pontoon.tv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

fun Fragment.createScreen(content: @Composable () -> Unit) = ComposeView(requireContext()).apply {
    setContent(content)
}