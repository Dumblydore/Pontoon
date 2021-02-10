package me.mauricee.pontoon.ui.login

import dagger.hilt.DefineComponent
import dagger.hilt.android.components.ActivityRetainedComponent

@DefineComponent(parent = ActivityRetainedComponent::class)
interface LoginComponent {
    @DefineComponent.Builder
    interface Builder{
        fun build(): LoginComponent
    }
}