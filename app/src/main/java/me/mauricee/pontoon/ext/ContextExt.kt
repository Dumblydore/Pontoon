package me.mauricee.pontoon.ext

import android.content.Context
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

fun Context.mainExecutor(): Executor = ContextCompat.getMainExecutor(this)