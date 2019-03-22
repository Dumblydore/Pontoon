package me.mauricee.pontoon.common.gestures

/**
 * Created by Burhanuddin Rashid on 2/27/2018.
 */

/**
 * Direction sealed class used as enum for defining directions
 */
sealed class Direction {
    object Left : Direction()
    object Right : Direction()
    object Up : Direction()
    object Down : Direction()
    object None : Direction()
}