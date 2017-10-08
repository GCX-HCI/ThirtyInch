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

package net.grandcentrix.thirtyinch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.util.Log;
import org.junit.*;
import org.mockito.*;

public class TiLogTest {

    @Test
    public void dontCrashForNullLogger() throws Exception {
        TiLog.setLogger(null);
        TiLog.v("tag", "msg");
        TiLog.i("tag", "msg");
        TiLog.d("tag", "msg");
        TiLog.e("tag", "msg");
        TiLog.w("tag", "msg");
        TiLog.log(Log.VERBOSE, "tag", "msg");
    }

    @Test
    public void logDToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.d("tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.DEBUG);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void logEToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.e("tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.ERROR);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void logIToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.i("tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.INFO);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void logVToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.v("tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.VERBOSE);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void logWToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.w("tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.WARN);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void loglogDToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.log(Log.DEBUG, "tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.DEBUG);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void loglogVToLogger() throws Exception {
        final TiLog.Logger logger = mock(TiLog.Logger.class);
        TiLog.setLogger(logger);
        final ArgumentCaptor<Integer> levelCaptor = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        TiLog.log(Log.VERBOSE, "tag", "msg");
        verify(logger).log(levelCaptor.capture(), tagCaptor.capture(), msgCaptor.capture());

        assertThat(levelCaptor.getValue()).isEqualTo(Log.VERBOSE);
        assertThat(tagCaptor.getValue()).isEqualTo("tag");
        assertThat(msgCaptor.getValue()).isEqualTo("msg");
    }

    @Test
    public void preventSettingRecursiveLogger() throws Exception {
        try {
            TiLog.setLogger(TiLog.TI_LOG);
            fail("did not throw");
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Recursion");
        }
    }
}