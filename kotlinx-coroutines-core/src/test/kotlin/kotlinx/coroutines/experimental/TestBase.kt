/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.coroutines.experimental

import org.junit.After
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

open class TestBase {
    var actionIndex = AtomicInteger()
    var finished = AtomicBoolean()
    var error = AtomicReference<Throwable>()

    public fun error(message: Any): Nothing {
        val exception = IllegalStateException(message.toString())
        error.compareAndSet(null, exception)
        throw exception
    }

    public inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
        if (!value) error(lazyMessage())
    }

    fun expect(index: Int) {
        val wasIndex = actionIndex.incrementAndGet()
        check(index == wasIndex) { "Expecting action index $index but it is actually $wasIndex" }
    }

    fun expectUnreached() {
        error("Should not be reached")
    }

    fun finish(index: Int) {
        expect(index)
        check(!finished.getAndSet(true)) { "Should call 'finish(...)' at most once" }
    }

    @After
    fun onCompletion() {
        error.get()?.let { throw it }
        check(actionIndex.get() == 0 || finished.get()) { "Expecting that 'finish(...)' was invoked, but it was not" }
    }

}