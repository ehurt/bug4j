/*
 * Copyright 2012 Cedric Dandoy
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
package org.bug4j.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class StringUtilTest {
    @Test
    public void testChop() throws Exception {
        Assert.assertEquals("", StringUtil.chopLenghtyString("", 10));
        Assert.assertEquals("abc", StringUtil.chopLenghtyString("abc", 10));
        Assert.assertEquals("abc def", StringUtil.chopLenghtyString("abc def", 10));

        Assert.assertEquals("abcdefghij klmnopqrst", StringUtil.chopLenghtyString("abcdefghijklmnopqrst", 10));
        Assert.assertEquals("abcdefghij klmnopqrst abcdefghij klmnopqrst", StringUtil.chopLenghtyString("abcdefghijklmnopqrstabcdefghijklmnopqrst", 10));
    }
}
