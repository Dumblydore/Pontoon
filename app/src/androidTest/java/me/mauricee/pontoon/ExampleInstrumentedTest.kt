package me.mauricee.pontoon

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Video of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("me.mauricee.pontoon", appContext.packageName)
    }
}
