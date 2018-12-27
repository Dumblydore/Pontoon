package me.mauricee.pontoon.me.mauricee.pontoon.rule

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class SchedulerRule : TestRule {
    private val testScheduler = Schedulers.trampoline()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { testScheduler }

                RxJavaPlugins.reset()
                RxJavaPlugins.setIoSchedulerHandler { testScheduler }
                RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }
                RxJavaPlugins.setComputationSchedulerHandler { testScheduler }

                base.evaluate()

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()
            }
        }
    }
}