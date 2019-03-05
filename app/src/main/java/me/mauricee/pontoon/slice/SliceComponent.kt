package me.mauricee.pontoon.slice

import dagger.Component
import me.mauricee.pontoon.di.AppComponent

@SliceScope
@Component(dependencies = [AppComponent::class])
interface SliceComponent {
    fun inject(provider: SlicesProvider)
}