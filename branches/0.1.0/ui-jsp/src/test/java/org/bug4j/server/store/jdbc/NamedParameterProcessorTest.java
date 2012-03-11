/*
 * Copyright 2011 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bug4j.server.store.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 */
public class NamedParameterProcessorTest {
    @Test
    public void test() {
        final NamedParameterProcessor namedParameterProcessor = new NamedParameterProcessor("abc :aaa and :2 def and :aaa blabla");
        Assert.assertEquals("abc ? and ? def and ? blabla", namedParameterProcessor.getJdbcSql());

        final List<Integer> aaa = namedParameterProcessor.getParameterPositions("aaa");
        Assert.assertArrayEquals(new Integer[]{1, 3}, aaa.toArray(new Integer[aaa.size()]));

        final List<Integer> two = namedParameterProcessor.getParameterPositions("2");
        Assert.assertArrayEquals(new Integer[]{2}, two.toArray(new Integer[two.size()]));

        final List<Integer> ccc = namedParameterProcessor.getParameterPositions("ccc");
        Assert.assertTrue(ccc.isEmpty());
    }
}
