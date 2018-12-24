/*
 * Copyright (C) 2017 grandcentrix GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.grandcentrix.thirtyinch

import android.util.Log
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.*
import org.junit.*

class TiLogTest {

    @Test
    fun `do not crash for null logger`() {
        TiLog.setLogger(null)
        TiLog.v("tag", "msg")
        TiLog.i("tag", "msg")
        TiLog.d("tag", "msg")
        TiLog.e("tag", "msg")
        TiLog.w("tag", "msg")
        TiLog.log(Log.VERBOSE, "tag", "msg")
    }

    @Test
    fun `log d to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.d("tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.DEBUG)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log e to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.e("tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.ERROR)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log i to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.i("tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.INFO)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log v to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.v("tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.VERBOSE)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log w to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.w("tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.WARN)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log Log_DEBUG to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.log(Log.DEBUG, "tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.DEBUG)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `log Log_VERBOSE to logger`() {
        val logger = mockk<TiLog.Logger>(relaxUnitFun = true)
        TiLog.setLogger(logger)
        val levelSlot = slot<Int>()
        val tagSlot = slot<String>()
        val msgSlot = slot<String>()

        TiLog.log(Log.VERBOSE, "tag", "msg")
        verify { logger.log(capture(levelSlot), capture(tagSlot), capture(msgSlot)) }

        assertThat(levelSlot.captured).isEqualTo(Log.VERBOSE)
        assertThat(tagSlot.captured).isEqualTo("tag")
        assertThat(msgSlot.captured).isEqualTo("msg")
    }

    @Test
    fun `prevent setting recursive logger`() {
        try {
            TiLog.setLogger(TiLog.TI_LOG)
            fail("did not throw")
        } catch (e: Exception) {
            assertThat(e).hasMessageContaining("Recursion")
        }
    }
}