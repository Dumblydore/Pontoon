package me.mauricee.pontoon.ui.main.player.playback

import dagger.Module
import dagger.Provides

@Module
object PlayerModule {
    @Provides
    fun PlayerFragment.providesArgs(): PlayerContract.Arguments = requireArguments().getParcelable(PlayerContract.Arguments.Key)!!
}