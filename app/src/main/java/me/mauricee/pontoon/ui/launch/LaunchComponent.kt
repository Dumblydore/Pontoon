package me.mauricee.pontoon.ui.launch

import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ActivityRetainedComponent

@DefineComponent(parent = ActivityRetainedComponent::class)
interface LaunchComponent {
    @DefineComponent.Builder
    interface Builder{
        fun build(): LaunchComponent
    }
}