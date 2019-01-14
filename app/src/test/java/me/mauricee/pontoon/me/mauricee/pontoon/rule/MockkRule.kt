package me.mauricee.pontoon.me.mauricee.pontoon.rule

import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

class MockkRule(private val overrideRecordPrivateCalls: Boolean = false, private val relaxUnitFun: Boolean = false, private val relaxed: Boolean = false) : MethodRule {
    override fun apply(base: Statement, method: FrameworkMethod, target: Any): Statement {
        return object : Statement() {
            override fun evaluate() {
                MockKAnnotations.init(target, overrideRecordPrivateCalls = overrideRecordPrivateCalls, relaxUnitFun = relaxUnitFun, relaxed = relaxed)
                base.evaluate()
                unmockkAll()
            }

        }
    }
}