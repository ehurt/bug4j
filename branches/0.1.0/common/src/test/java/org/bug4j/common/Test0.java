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

package org.bug4j.common;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Test0 {
    @Test
    public void test() throws Exception {
        doit("20120109100603", "yyyyMMddHHmmss");
        doit("20120109100603177", "yyyyMMddHHmmssSSS");
        doit("2012/01/09 10:06:03", "y/M/d H:m:s");
        doit("2012/01/09 10:06:03:177", "y/M/d H:m:s:S");
        Thread.sleep(100);
    }

    private void doit(String input, String pattern) throws ParseException {
        System.out.printf(
                "%d - '%s' - '%s' - %s\n",
                input.length(),
                pattern,
                input,
                new SimpleDateFormat(pattern).parse(input)
        );
    }
}
