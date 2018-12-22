package me

import io.mockk.ConstantAnswer
import io.mockk.MockKStubScope
import io.reactivex.Observable

//fun <T, B> MockKStubScope<T, B>.returnsStream(vararg returnValues: T) = answers<Observable<T>,B>(Observable.fromArray(returnValues))