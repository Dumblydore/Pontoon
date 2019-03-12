package me.mauricee.pontoon.worker

import androidx.work.Worker
import dagger.Subcomponent
import me.mauricee.pontoon.Pontoon

@WorkScope
@Subcomponent
interface WorkerComponent {
    fun inject(worker: LiveStreamWorker)
}

fun Worker.appComponent() = (applicationContext as Pontoon).appComponent