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

package org.bug4j.client;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class TextToLinesTest {
    /**
     * Test with LF
     */
    @Test
    public void testLf() throws Exception {
        final String[] strings = TextToLines.toLineArray("line1\nline2\nline3\n");
        Assert.assertArrayEquals(new String[]{"line1", "line2", "line3"}, strings);
    }

    /**
     * Test with CR+LF
     */
    @Test
    public void testCrLf() throws Exception {
        final String[] strings = TextToLines.toLineArray("line1\r\nline2\r\nline3\r\n");
        Assert.assertArrayEquals(new String[]{"line1", "line2", "line3"}, strings);
    }

    /**
     * Test without an ending LF
     */
    @Test
    public void testLfNoLast() throws Exception {
        final String[] strings = TextToLines.toLineArray("line1\nline2\nline3");
        Assert.assertArrayEquals(new String[]{"line1", "line2", "line3"}, strings);
    }

    /**
     * Test without an ending CR+LF
     */
    @Test
    public void testCrLfNoLast() throws Exception {
        final String[] strings = TextToLines.toLineArray("line1\r\nline2\r\nline3");
        Assert.assertArrayEquals(new String[]{"line1", "line2", "line3"}, strings);
    }
}
